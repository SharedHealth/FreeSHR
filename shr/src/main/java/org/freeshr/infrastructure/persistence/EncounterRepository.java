package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Date;
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
        cqlOperations.executeAsynchronously("INSERT INTO encounter (encounter_id, health_id, date, content,division_id, district_id, upazilla_id, city_corporation_id,ward_id) VALUES ( '"+encounterBundle.getEncounterId() + "','"
                + encounterBundle.getHealthId() + "','"
                + getCurrentTime() + "','"
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

    public ListenableFuture<List<EncounterBundle>> findAllEncountersByCatchment(String columnValue , String columnName) {
        return executeFindQuery("SELECT * FROM encounter WHERE " + columnName + "='" + columnValue + "';");
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
                    bundle.setDate(result.getString("date"));
                    bundle.setEncounterContent(result.getString("content"));
                    bundles.add(bundle);
                }
                return bundles;
            }
        };
    }


    private String getCurrentTime() {
        return String.format("%tFT%<tRZ", new Date());
    }

}
