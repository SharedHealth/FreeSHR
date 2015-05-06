package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.events.EncounterEvent;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.events.EncounterEventLog;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static ch.lambdaj.Lambda.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static java.lang.String.format;
import static org.freeshr.infrastructure.persistence.RxMaps.completeResponds;
import static org.freeshr.infrastructure.persistence.RxMaps.respondOnNext;
import static org.freeshr.utils.Confidentiality.getConfidentiality;

@Component
public class EncounterRepository {
    private static final Logger logger = LoggerFactory.getLogger(EncounterRepository.class);

    private CqlOperations cqlOperations;
    private String fhirDocumentSchemaVersion;

    @Autowired
    public EncounterRepository(@Qualifier("SHRCassandraTemplate") CqlOperations cassandraTemplate, SHRProperties shrProperties) {
        this.cqlOperations = cassandraTemplate;
        fhirDocumentSchemaVersion = shrProperties.getFhirDocumentSchemaVersion();
    }

    public Observable<Boolean> save(EncounterBundle encounterBundle, Patient patient) {
        Address address = patient.getAddress();
        UUID receivedTimeUUID = TimeUuidUtil.uuidForDate(encounterBundle.getReceivedAt());
        UUID updatedTimeUUID = TimeUuidUtil.uuidForDate(encounterBundle.getUpdatedAt());

        Insert insertEncounterStmt = getInsertEncounterStmt(encounterBundle, receivedTimeUUID, updatedTimeUUID);

        RegularStatement encCatchmentStmt = getInsertEncCatchmentStmt(encounterBundle, address, receivedTimeUUID);
        RegularStatement encByPatientStmt = getInsertEncByPatientStmt(encounterBundle, receivedTimeUUID);

        Batch batch = QueryBuilder.batch(insertEncounterStmt, encCatchmentStmt, encByPatientStmt);
        Observable<ResultSet> saveObservable = Observable.from(cqlOperations.executeAsynchronously(batch),
                Schedulers.io());

        return saveObservable.flatMap(respondOnNext(true), RxMaps.<Boolean>logAndForwardError(logger),
                completeResponds(true));
    }

    public Observable<List<EncounterEvent>> findEncounterFeedForCatchment(Catchment catchment, Date updatedSince,
                                                                          int limit) {
        String identifyEncounterIdsQuery = buildCatchmentSearchQuery(catchment, updatedSince, limit);
        Observable<ResultSet> encounterIdsObservable = Observable.from(
                cqlOperations.queryAsynchronously(identifyEncounterIdsQuery), Schedulers.io());

        return encounterIdsObservable.concatMap(findEncountersOrderedByEvents());
    }

    public Observable<EncounterBundle> findEncounterById(String encounterId) {
        Select findEncounter = QueryBuilder
                .select("encounter_id", "health_id", "received_at", getContentColumnName(), "created_by",
                        "updated_at", "updated_by", "content_version", getSchemaContentVersionColumnName(),
                        "encounter_confidentiality", "patient_confidentiality")
                .from("encounter")
                .where(eq("encounter_id", encounterId))
                .limit(1);

        return Observable.from(cqlOperations.queryAsynchronously(findEncounter), Schedulers.io()).flatMap(
                new Func1<ResultSet, Observable<EncounterBundle>>() {
                    @Override
                    public Observable<EncounterBundle> call(ResultSet rows) {
                        List<EncounterBundle> encounterBundles = read(rows);
                        if (encounterBundles.isEmpty()) {
                            return Observable.empty();
                        } else {
                            return Observable.just(encounterBundles.get(0));
                        }
                    }
                });
    }

    public Observable<List<EncounterEvent>> findEncounterFeedForPatient(String healthId, Date updatedSince,
                                                                        int limit) throws ExecutionException,
            InterruptedException {
        StringBuilder queryBuilder = buildQuery(healthId, updatedSince, limit);
        Observable<ResultSet> encounterIdsWithCreatedTimeObservable = Observable.from(cqlOperations.queryAsynchronously
                (queryBuilder.toString()), Schedulers.io());

        return encounterIdsWithCreatedTimeObservable.concatMap(findEncountersOrderedByEvents());
    }

    public Observable<Boolean> updateEncounter(EncounterBundle encounterBundle, EncounterBundle existingEncounterBundle, Patient patient) {
        Address address = patient.getAddress();
        UUID receivedTimeUUID = TimeUuidUtil.uuidForDate(existingEncounterBundle.getReceivedAt());
        UUID updatedTimeUUID = TimeUuidUtil.uuidForDate(encounterBundle.getUpdatedAt());

        Update updateEncounterStmt = getUpdateEncounterStmt(encounterBundle, receivedTimeUUID, updatedTimeUUID);
        RegularStatement encCatchmentStmt = getInsertEncCatchmentStmt(encounterBundle, address, updatedTimeUUID);
        RegularStatement encByPatientStmt = getInsertEncByPatientStmt(encounterBundle, updatedTimeUUID);
        RegularStatement encounterHistoryStmt = getInsertEncHistory(existingEncounterBundle);

        Batch batch = QueryBuilder.batch(updateEncounterStmt, encCatchmentStmt, encByPatientStmt, encounterHistoryStmt);
        Observable<ResultSet> saveObservable = Observable.from(cqlOperations.executeAsynchronously(batch),
                Schedulers.io());

        return saveObservable.flatMap(respondOnNext(true), RxMaps.<Boolean>logAndForwardError(logger),
                completeResponds(true));

    }

    private Func1<ResultSet, Observable<List<EncounterEvent>>> findEncountersOrderedByEvents() {
        return new Func1<ResultSet, Observable<List<EncounterEvent>>>() {
            @Override
            public Observable<List<EncounterEvent>> call(ResultSet rows) {
                List<Row> encounterEventLogRecords = rows.all();
                List<EncounterEventLog> encounterEventLogs = new ArrayList<>();
                EncounterEventLog encounterEventLog;
                for (Row result : encounterEventLogRecords) {
                    encounterEventLog = new EncounterEventLog();
                    encounterEventLog.setEncounterId(result.getString("encounter_id"));
                    encounterEventLog.setCreatedAt(result.getUUID("created_at"));

                    encounterEventLogs.add(encounterEventLog);
                }
                LinkedHashSet<String> encounterIds = new LinkedHashSet<>(extract(encounterEventLogs, on(EncounterEventLog.class).getEncounterId()));
                Observable<List<EncounterBundle>> encounterBundleObservable = findEncounters(encounterIds);

                return encounterBundleObservable.concatMap(generateEncounterEvents(encounterEventLogs));
            }
        };
    }

    private Func1<List<EncounterBundle>, Observable<List<EncounterEvent>>> generateEncounterEvents(final List<EncounterEventLog> encounterInstances) {
        return new Func1<List<EncounterBundle>, Observable<List<EncounterEvent>>>() {
            @Override
            public Observable<List<EncounterEvent>> call(List<EncounterBundle> encounterBundles) {
            List<EncounterEvent> encounterEvents = new ArrayList<>();
                EncounterEvent encounterEvent;
                for (EncounterEventLog encounterInstance : encounterInstances) {
                    encounterEvent = new EncounterEvent();
                    EncounterBundle savedEncounterBundle = selectFirst(encounterBundles, having(on(EncounterBundle.class).getEncounterId(),
                            Matchers.equalTo(encounterInstance.getEncounterId())));

                    encounterEvent.setUpdatedAt(TimeUuidUtil.getDateFromUUID(encounterInstance.getCreatedAt()));
                    encounterEvent.setEncounterBundle(savedEncounterBundle);
                    encounterEvents.add(encounterEvent);
                }
                return Observable.just(encounterEvents);
            }

        };
    }

    private RegularStatement getInsertEncHistory(EncounterBundle encounterBundle) {
        Insert insertEncHistoryStmt = QueryBuilder.insertInto("enc_history");
        insertEncHistoryStmt.value("encounter_id", encounterBundle.getEncounterId());
        insertEncHistoryStmt.value("encounter_updated_at", TimeUuidUtil.uuidForDate(encounterBundle.getUpdatedAt()));
        insertEncHistoryStmt.value("content_version", encounterBundle.getContentVersion());
        insertEncHistoryStmt.value("content_format", fhirDocumentSchemaVersion);
        insertEncHistoryStmt.value("content", encounterBundle.getContent());

        return insertEncHistoryStmt;
    }

    private Update getUpdateEncounterStmt(EncounterBundle encounterBundle, UUID receivedTimeUUID, UUID updatedTimeUUID) {
        Update updateEncounterStmt = QueryBuilder.update("encounter");

        updateEncounterStmt.with(QueryBuilder.set(getContentColumnName(), encounterBundle.getContent()))
                .and(QueryBuilder.set("content_version", encounterBundle.getContentVersion()))
                .and(QueryBuilder.set(getSchemaContentVersionColumnName(), encounterBundle.getContentVersion()))
                .and(QueryBuilder.set("encounter_confidentiality", encounterBundle.getEncounterConfidentiality().getLevel()))
                .and(QueryBuilder.set("updated_at", updatedTimeUUID))
                .and(QueryBuilder.set("updated_by", serializeRequester(encounterBundle.getUpdatedBy())))
                .where(QueryBuilder.eq("encounter_id", encounterBundle.getEncounterId()))
                .and(QueryBuilder.eq("received_at", receivedTimeUUID));

        return updateEncounterStmt;

    }

    private Insert getInsertEncounterStmt(EncounterBundle encounterBundle, UUID receivedTimeUUID, UUID updatedTimeUUID) {
        Insert insertEncounterStmt = QueryBuilder.insertInto("encounter");
        insertEncounterStmt.value("encounter_id", encounterBundle.getEncounterId());
        insertEncounterStmt.value("health_id", encounterBundle.getHealthId());
        insertEncounterStmt.value("received_at", receivedTimeUUID);
        insertEncounterStmt.value("created_by", serializeRequester(encounterBundle.getCreatedBy()));
        insertEncounterStmt.value("content_version", encounterBundle.getContentVersion());
        insertEncounterStmt.value(getSchemaContentVersionColumnName(), encounterBundle.getContentVersion());
        insertEncounterStmt.value(getContentColumnName(), encounterBundle.getEncounterContent().toString());
        insertEncounterStmt.value("encounter_confidentiality", encounterBundle.getEncounterConfidentiality().getLevel());
        insertEncounterStmt.value("patient_confidentiality", encounterBundle.getPatientConfidentiality().getLevel());
        insertEncounterStmt.value("updated_at", updatedTimeUUID);
        insertEncounterStmt.value("updated_by", serializeRequester(encounterBundle.getUpdatedBy()));
        return insertEncounterStmt;
    }

    private RegularStatement getInsertEncCatchmentStmt(EncounterBundle encounterBundle, Address address, UUID createdTimeUUID) {
        String encCatchmentInsertQuery =
                format("INSERT INTO enc_by_catchment (division_id, district_id, year, " +
                                " created_at, upazila_id, city_corporation_id, union_urban_ward_id, encounter_id) " +
                                " values ('%s', '%s', %s, %s, '%s', '%s', '%s', '%s');",
                        address.getDivision(),
                        address.getConcatenatedDistrictId(),
                        DateUtil.getCurrentYear(),
                        createdTimeUUID,
                        address.getConcatenatedUpazilaId(),
                        StringUtils.defaultString(address.getConcatenatedCityCorporationId()),
                        StringUtils.defaultString(address.getConcatenatedWardId()),
                        encounterBundle.getEncounterId());
        return new SimpleStatement(encCatchmentInsertQuery);
    }

    private RegularStatement getInsertEncByPatientStmt(EncounterBundle encounterBundle, UUID createdTimeUUID) {
        String encByPatientInsertQuery =
                format("INSERT INTO enc_by_patient (health_id, created_at, encounter_id) " +
                                " VALUES ('%s', %s, '%s')", encounterBundle.getHealthId(), createdTimeUUID,
                        encounterBundle.getEncounterId());
        return new SimpleStatement(encByPatientInsertQuery);
    }

    private String serializeRequester(Requester requester) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(requester);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String getSchemaContentVersionColumnName() {
        return format("content_version_%s", fhirDocumentSchemaVersion);
    }

    private String buildCatchmentSearchQuery(Catchment catchment, Date updatedSince, int limit) {
        int yearOfDate = DateUtil.getYearOf(updatedSince);
        String lastUpdateTime = DateUtil.toDateString(updatedSince, DateUtil.UTC_DATE_IN_MILLIS_FORMAT);
        //TODO test. condition should be >=
        return format("SELECT encounter_id, created_at FROM enc_by_catchment " +
                        " WHERE year = %s and created_at >= minTimeUuid('%s') and %s LIMIT %s ALLOW FILTERING;",
                yearOfDate, lastUpdateTime, buildClauseForCatchment(catchment), limit);
    }

    private Observable<List<EncounterBundle>> findEncounters(LinkedHashSet<String> encounterIds) {
        if (encounterIds.isEmpty()) {
            List<EncounterBundle> empty = new ArrayList<>();
            return Observable.just(empty);
        }
        String encounterQuery = buildEncounterSelectionQuery(encounterIds);
        return executeFindQuery(encounterQuery);
    }

    private String buildEncounterSelectionQuery(LinkedHashSet<String> encounterIds) {
        StringBuilder encounterQuery = new StringBuilder("SELECT encounter_id, health_id, received_at, " +
                getContentColumnName() + ", created_by, updated_at, updated_by, content_version, " +
                getSchemaContentVersionColumnName() + ", " +
                "patient_confidentiality, encounter_confidentiality FROM encounter where encounter_id in (");
        int noOfEncounters = encounterIds.size();
        int idx = 0;
        for (String encounterId : encounterIds) {
            encounterQuery.append("'").append(encounterId).append("'");
            idx++;
            if (idx < noOfEncounters) {
                encounterQuery.append(",");
            }
        }
        encounterQuery.append(")");
        return encounterQuery.toString();
    }

    private String getContentColumnName() {
        return format("content_%s", fhirDocumentSchemaVersion);
    }

    private String buildClauseForCatchment(Catchment catchment) {
        int level = catchment.getLevel();
        String clause = "";
        for (int l = 1; l <= level; l++) {
            String levelType = catchment.levelType(l);
            if (!StringUtils.isBlank(levelType)) {
                clause = clause + format("%s = '%s'", levelType, catchment.levelCode(l));
                if (l < level) {
                    clause = clause + " and ";
                }
            }
        }
        return clause;
    }

    private Observable<List<EncounterBundle>> executeFindQuery(final String cql) {
        return Observable.from(cqlOperations.queryAsynchronously(cql), Schedulers.io()).map(new Func1<ResultSet,
                List<EncounterBundle>>() {
            @Override
            public List<EncounterBundle> call(ResultSet rows) {
                return read(rows);
            }
        });
    }

    private List<EncounterBundle> read(ResultSet resultSet) {
        List<EncounterBundle> bundles = new ArrayList<>();
        List<Row> rows = resultSet.all();
        for (Row result : rows) {
            EncounterBundle bundle = new EncounterBundle();
            bundle.setEncounterId(result.getString("encounter_id"));
            bundle.setHealthId(result.getString("health_id"));
            bundle.setReceivedAt(TimeUuidUtil.getDateFromUUID(result.getUUID("received_at")));
            bundle.setCreatedBy(getRequesterValue(result, "created_by"));
            bundle.setEncounterContent(result.getString(getContentColumnName()));
            bundle.setEncounterConfidentiality(getConfidentiality(result.getString("encounter_confidentiality")));
            bundle.setPatientConfidentiality(getConfidentiality(result.getString("patient_confidentiality")));
            bundle.setUpdatedBy(getRequesterValue(result, "updated_by"));
            bundle.setUpdatedAt(TimeUuidUtil.getDateFromUUID(result.getUUID("updated_at")));
            bundle.setContentVersion(result.getInt("content_version"));
            bundles.add(bundle);
        }
        return bundles;
    }

    private Requester getRequesterValue(Row result, String colName) {
        try {
            return new ObjectMapper().readValue(result.getString(colName), Requester.class);
        } catch (IOException e) {
            return null;
        }
    }

    private StringBuilder buildQuery(String healthId, Date updatedSince, int limit) {
        StringBuilder queryBuilder = new StringBuilder(format("SELECT encounter_id, created_at FROM enc_by_patient where " +
                "health_id='%s'", healthId));
        if (updatedSince != null) {
            String lastUpdateTime = DateUtil.toDateString(updatedSince, DateUtil.UTC_DATE_IN_MILLIS_FORMAT);
            queryBuilder.append(format(" and created_at >= minTimeUuid('%s')", lastUpdateTime));
        }
        queryBuilder.append(format(" LIMIT %d;", limit));
        return queryBuilder;
    }

}
