package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.domain.model.Catchment;
import org.freeshr.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class EncounterRepository {
    private static final Logger logger = LoggerFactory.getLogger(EncounterRepository.class);

    private CqlOperations cqlOperations;

    @Autowired
    public EncounterRepository(@Qualifier("SHRCassandraTemplate") CqlOperations cassandraTemplate) {
        this.cqlOperations = cassandraTemplate;
    }

    public void save(EncounterBundle encounterBundle, Patient patient) throws ExecutionException, InterruptedException {
        Address address = patient.getAddress();

        Insert insertEncounterStmt = QueryBuilder.insertInto("encounter");
        insertEncounterStmt.value("encounter_id", encounterBundle.getEncounterId());
        insertEncounterStmt.value("health_id", encounterBundle.getHealthId());
        insertEncounterStmt.value("received_date", DateUtil.getCurrentTimeInISOString()); //TODO check timefunction
        insertEncounterStmt.value("content", encounterBundle.getEncounterContent().toString());
        insertEncounterStmt.value("patient_location_code", address.getLocationCode());

        String encCatchmentInsertQuery =
                String.format("INSERT INTO enc_by_catchment (division_id, district_id, year, " +
                " received_date, upazila_id, city_corporation_id, union_urban_ward_id, encounter_id) " +
                " values ('%s', '%s', %s, now(), '%s', '%s', '%s', '%s');",
                        address.getDivision(),
                        address.getConcatenatedDistrictId(),
                        DateUtil.getCurrentYear(),
                        address.getConcatenatedUpazilaId(),
                        StringUtils.defaultString(address.getConcatenatedCityCorporationId()),
                        StringUtils.defaultString(address.getConcatenatedWardId()),
                        encounterBundle.getEncounterId());
        RegularStatement encCatchmentStmt = new SimpleStatement(encCatchmentInsertQuery);
        Batch batch = QueryBuilder.batch(insertEncounterStmt, encCatchmentStmt);
        System.out.println(batch.toString());
        cqlOperations.execute(batch);
    }

    public List<EncounterBundle> findEncountersForCatchment(Catchment catchment, Date updatedSince, int limit) throws ExecutionException, InterruptedException {
        String identifyEncountersQuery = buildCatchmentSearchQuery(catchment, updatedSince, limit);
        ResultSet resultSet = cqlOperations.query(identifyEncountersQuery);
        List<Row> rows = resultSet.all();
        LinkedHashSet<String> encounterIds = new LinkedHashSet<>();
        for (Row result : rows) {
            encounterIds.add(result.getString("encounter_id"));
        }
        return findEncounters(encounterIds);

    }

    private String buildCatchmentSearchQuery(Catchment catchment, Date updatedSince, int limit) {
        int yearOfDate = DateUtil.getYearOf(updatedSince);
        String lastUpdateTime = new SimpleDateFormat(DateUtil.UTC_DATE_IN_MILLIS_FORMAT).format(updatedSince);
        //TODO test. condition should be >=
        return String.format("SELECT encounter_id FROM enc_by_catchment " +
                      " WHERE year = %s and received_date > minTimeUuid('%s') and %s LIMIT %s ALLOW FILTERING;",
                        yearOfDate, lastUpdateTime, buildClauseForCatchment(catchment), limit);
    }

    private List<EncounterBundle> findEncounters(LinkedHashSet<String> encounterIds) throws ExecutionException, InterruptedException {
        String encounterQuery = buildEncounterSelectionQuery(encounterIds);
        return executeFindQuery(encounterQuery);
    }

    private String buildEncounterSelectionQuery(LinkedHashSet<String> encounterIds) {
        StringBuffer encounterQuery = new StringBuffer("SELECT encounter_id, health_id, received_date, content FROM encounter where encounter_id in (");
        int noOfEncounters = encounterIds.size();
        int idx = 0;
        for (String encounterId : encounterIds) {
            encounterQuery.append("'" + encounterId + "'");
            idx++;
            if (idx < noOfEncounters) {
                encounterQuery.append(",");
            }
        }
        encounterQuery.append(")");
        return encounterQuery.toString();
    }

    private String buildClauseForCatchment(Catchment catchment) {
        int level = catchment.getLevel();
        String clause = "";
        for (int l = 1; l <= level; l++) {
            String levelType = catchment.levelType(l);
            if (!StringUtils.isBlank(levelType)) {
                clause = clause + String.format("%s = '%s'", levelType, catchment.levelCode(l));
                if (l < level) {
                    clause = clause + " and ";
                }
            }
        }
        return clause;
    }

    /**
     *
     * @param catchment
     * @param catchmentType
     * @param date
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * @deprecated do not use this method
     * @see #findEncountersForCatchment(org.freeshr.domain.model.Catchment, java.util.Date, int)
     */
    public List<EncounterBundle> findAllEncountersByCatchment(String catchment, String catchmentType, String date) throws ExecutionException, InterruptedException {
        String query = String.format("SELECT encounter_id, health_id, received_date, content " +
                "FROM encounter WHERE %s = '%s' and received_date > '%s'; ", catchmentType, catchment, date);
        return executeFindQuery(query);
    }

    private List<EncounterBundle> executeFindQuery(final String cql) throws ExecutionException, InterruptedException {
        ResultSet resultSet = cqlOperations.query(cql);
        return read(resultSet);
    }

    private List<EncounterBundle> read(ResultSet resultSet) throws ExecutionException {
        //TODO return LinkedHashSet
        List<EncounterBundle> bundles = new ArrayList<>();
        List<Row> rows = resultSet.all();
        for (Row result : rows) {
            EncounterBundle bundle = new EncounterBundle();
            bundle.setEncounterId(result.getString("encounter_id"));
            bundle.setHealthId(result.getString("health_id"));
            bundle.setReceivedDate(DateUtil.toISOString(result.getDate("received_date")));
            bundle.setEncounterContent(result.getString("content"));
            bundles.add(bundle);
        }
        return bundles;
    }

    /**
     *
     * @param healthId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * @deprecated do not use this query.
     */
    public List<EncounterBundle> findAll(String healthId) throws ExecutionException, InterruptedException {
        //TODO refactor
        return executeFindQuery("SELECT encounter_id, health_id, received_date, content " +
                "FROM encounter WHERE health_id='" + healthId + "';");
    }


}
