package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
        UUID receivedTimeUUID = TimeUuidUtil.uuidForDate(encounterBundle.getReceivedDate());
        UUID updatedTimeUUID = TimeUuidUtil.uuidForDate(encounterBundle.getUpdatedDate());

        Insert insertEncounterStmt = QueryBuilder.insertInto("encounter");
        insertEncounterStmt.value("encounter_id", encounterBundle.getEncounterId());
        insertEncounterStmt.value("health_id", encounterBundle.getHealthId());
        insertEncounterStmt.value("received_date", receivedTimeUUID); //TODO check timefunction
        insertEncounterStmt.value("created_by", serializeRequester(encounterBundle.getCreatedBy())); //TODO check timefunction
        insertEncounterStmt.value("content_version", encounterBundle.getContentVersion());
        insertEncounterStmt.value(getSchemaContentVersionColumnName(), encounterBundle.getContentVersion());
        insertEncounterStmt.value(getContentColumnName(), encounterBundle.getEncounterContent().toString());
        insertEncounterStmt.value("encounter_confidentiality", encounterBundle.getEncounterConfidentiality().getLevel());
        insertEncounterStmt.value("patient_confidentiality", encounterBundle.getPatientConfidentiality().getLevel());
        insertEncounterStmt.value("updated_date", updatedTimeUUID);
        insertEncounterStmt.value("updated_by", serializeRequester(encounterBundle.getUpdatedBy()));

        String encCatchmentInsertQuery =
                format("INSERT INTO enc_by_catchment (division_id, district_id, year, " +
                                " received_date, upazila_id, city_corporation_id, union_urban_ward_id, encounter_id) " +
                                " values ('%s', '%s', %s, %s, '%s', '%s', '%s', '%s');",
                        address.getDivision(),
                        address.getConcatenatedDistrictId(),
                        DateUtil.getCurrentYear(),
                        receivedTimeUUID,
                        address.getConcatenatedUpazilaId(),
                        StringUtils.defaultString(address.getConcatenatedCityCorporationId()),
                        StringUtils.defaultString(address.getConcatenatedWardId()),
                        encounterBundle.getEncounterId());
        RegularStatement encCatchmentStmt = new SimpleStatement(encCatchmentInsertQuery);
        String encByPatientInsertQuery =
                format("INSERT INTO enc_by_patient (health_id, received_date, encounter_id) " +
                                " VALUES ('%s', %s, '%s')", encounterBundle.getHealthId(), receivedTimeUUID,
                        encounterBundle.getEncounterId());
        RegularStatement encByPatientStmt = new SimpleStatement(encByPatientInsertQuery);

        Batch batch = QueryBuilder.batch(insertEncounterStmt, encCatchmentStmt, encByPatientStmt);
        Observable<ResultSet> saveObservable = Observable.from(cqlOperations.executeAsynchronously(batch),
                Schedulers.io());

        return saveObservable.flatMap(respondOnNext(true), RxMaps.<Boolean>logAndForwardError(logger),
                completeResponds(true));
    }

    public Observable<List<EncounterBundle>> findEncountersForCatchment(Catchment catchment, Date updatedSince,
                                                                        int limit) {
        String identifyEncountersQuery = buildCatchmentSearchQuery(catchment, updatedSince, limit);
        Observable<ResultSet> resultSetObservable = Observable.from(
                cqlOperations.queryAsynchronously(identifyEncountersQuery), Schedulers.io());

        return resultSetObservable.concatMap(new Func1<ResultSet, Observable<List<EncounterBundle>>>() {
            @Override
            public Observable<List<EncounterBundle>> call(ResultSet rows) {
                List<Row> encounterBundles = rows.all();
                LinkedHashSet<String> encounterIds = new LinkedHashSet<>();
                for (Row result : encounterBundles) {
                    encounterIds.add(result.getString("encounter_id"));
                }
                return findEncounters(encounterIds);
            }
        });
    }

    public Observable<EncounterBundle> findEncounterById(String encounterId) {
        Select findEncounter = QueryBuilder
                .select("encounter_id", "health_id", "received_date", getContentColumnName(), "created_by",
                        "updated_date", "updated_by", "content_version", getSchemaContentVersionColumnName(),
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

    public Observable<List<EncounterBundle>> findEncountersForPatient(String healthId, Date updatedSince,
                                                                      int limit) throws ExecutionException,
            InterruptedException {
        StringBuilder queryBuilder = buildQuery(healthId, updatedSince, limit);
        Observable<LinkedHashSet<String>> encounterIdsObservable = Observable.from(cqlOperations.queryAsynchronously
                (queryBuilder.toString()), Schedulers.io())
                .map(new Func1<ResultSet, LinkedHashSet<String>>() {
                    @Override
                    public LinkedHashSet<String> call(ResultSet rows) {
                        LinkedHashSet<String> encounterIds = new LinkedHashSet<>();
                        for (Row row : rows) {
                            encounterIds.add(row.getString("encounter_id"));
                        }
                        return encounterIds;
                    }
                });

        return encounterIdsObservable.flatMap(
                new Func1<LinkedHashSet<String>, Observable<List<EncounterBundle>>>() {
                    @Override
                    public Observable<List<EncounterBundle>> call(LinkedHashSet<String> encounterIds) {
                        if (encounterIds.isEmpty()) {
                            return Observable.<List<EncounterBundle>>just(new ArrayList<EncounterBundle>());
                        }
                        String encounterQuery = buildEncounterSelectionQuery(encounterIds);
                        return executeFindQuery(encounterQuery);
                    }
                });
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
        return format("SELECT encounter_id FROM enc_by_catchment " +
                        " WHERE year = %s and received_date >= minTimeUuid('%s') and %s LIMIT %s ALLOW FILTERING;",
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
        StringBuilder encounterQuery = new StringBuilder("SELECT encounter_id, health_id, received_date, " +
                getContentColumnName() + ", created_by, updated_date, updated_by, content_version, " +
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
            bundle.setReceivedDate(TimeUuidUtil.getDateFromUUID(result.getUUID("received_date")));
            bundle.setCreatedBy(getRequesterValue(result, "created_by"));
            bundle.setEncounterContent(result.getString(getContentColumnName()));
            bundle.setEncounterConfidentiality(getConfidentiality(result.getString("encounter_confidentiality")));
            bundle.setPatientConfidentiality(getConfidentiality(result.getString("patient_confidentiality")));
            bundle.setUpdatedBy(getRequesterValue(result, "updated_by"));
            bundle.setUpdatedDate(TimeUuidUtil.getDateFromUUID(result.getUUID("updated_date")));

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
        StringBuilder queryBuilder = new StringBuilder(format("SELECT encounter_id FROM enc_by_patient where " +
                "health_id='%s'", healthId));
        if (updatedSince != null) {
            String lastUpdateTime = DateUtil.toDateString(updatedSince, DateUtil.UTC_DATE_IN_MILLIS_FORMAT);
            queryBuilder.append(format(" and received_date >= minTimeUuid('%s')", lastUpdateTime));
        }
        queryBuilder.append(format(" LIMIT %d;", limit));
        return queryBuilder;
    }
}
