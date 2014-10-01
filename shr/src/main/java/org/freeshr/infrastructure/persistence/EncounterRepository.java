package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class EncounterRepository {
    private static final Logger logger = LoggerFactory.getLogger(EncounterRepository.class);

    private CqlOperations cqlOperations;
    private DateUtil dateUtil;

    @Autowired
    public EncounterRepository(@Qualifier("SHRCassandraTemplate") CqlOperations cassandraTemplate) {
        this.cqlOperations = cassandraTemplate;
        setDateUtil(new DateUtil());
    }

    void setDateUtil(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    public void save(EncounterBundle encounterBundle, Patient patient) throws ExecutionException, InterruptedException {
        Address address = patient.getAddress();
        cqlOperations.executeAsynchronously("INSERT INTO encounter (encounter_id, health_id, date, content,division_id, district_id, upazilla_id, city_corporation_id,ward_id) VALUES ( '" + encounterBundle.getEncounterId() + "','"
                + encounterBundle.getHealthId() + "','"
                + dateUtil.getCurrentTimeInUTC() + "','"
                + encounterBundle.getEncounterContent() + "','"
                + address.getDivision() + "','"
                + address.getConcatenatedDistrictId() + "','"
                + address.getConcatenatedUpazillaId() + "','"
                + address.getConcatenatedCityCorporationId() + "','"
                + address.getConcatenatedWardId() +
                "');");
    }

    public ListenableFuture<List<EncounterBundle>> findAll(String healthId) {
        return executeFindQuery("SELECT * FROM encounter WHERE health_id='" + healthId + "';");
    }

    public ListenableFuture<List<EncounterBundle>> findAllEncountersByCatchment(String columnValue , String columnName, String date){
        String query = String.format("SELECT * FROM encounter WHERE %s = '%s' and date > '%s'; ", columnName, columnValue, date);
        return executeFindQuery(query);
    }


    private ListenableFuture<List<EncounterBundle>> executeFindQuery(final String cql) {
        return new SimpleListenableFuture<List<EncounterBundle>, ResultSet>(
                cqlOperations.queryAsynchronously(cql)) {
            @Override
            protected List<EncounterBundle> adapt(ResultSet resultSet) throws ExecutionException {
                List<EncounterBundle> bundles = new ArrayList<EncounterBundle>();
                for (Row result : resultSet.all()) {
                    EncounterBundle bundle = new EncounterBundle();
                    bundle.setEncounterId(result.getString("encounter_id"));
                    bundle.setHealthId(result.getString("health_id"));
                    bundle.setDate(dateUtil.fromUTCDate(result.getDate("date")));
                    bundle.setEncounterContent(result.getString("content"));
                    bundles.add(bundle);
                }
                return bundles;
            }
        };
    }



}
