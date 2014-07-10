package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
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

    public void save(EncounterBundle encounterBundle) {
        cqlOperations.executeAsynchronously("INSERT INTO encounter (encounter_id, health_id, date, content) VALUES ('"
                + encounterBundle.getEncounterId() + "','"
                + encounterBundle.getHealthId() + "','"
                + getCurrentTime() + "','"
                + encounterBundle.getContent()
                + "');");
    }

    public ListenableFuture<List<EncounterBundle>> findAll(String healthId) {
        return new SimpleListenableFuture<List<EncounterBundle>, ResultSet>(
                cqlOperations.queryAsynchronously("SELECT * FROM encounter WHERE health_id='" + healthId + "';")) {
            @Override
            protected List<EncounterBundle> adapt(ResultSet resultSet) throws ExecutionException {
                List<EncounterBundle> bundles = new ArrayList<EncounterBundle>();
                for (Row result : resultSet.all()) {
                    EncounterBundle bundle = new EncounterBundle();
                    bundle.setEncounterId(result.getString("encounter_id"));
                    bundle.setHealthId(result.getString("health_id"));
                    bundle.setDate(result.getString("date"));
                    bundle.setContent(result.getString("content"));
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
