package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

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
                + encounterBundle.getDate() + "','"
                + encounterBundle.getContent()
                + "');");
    }

    public ListenableFuture<EncounterBundle> findByHealthId(String healthId) {
        return new SimpleListenableFuture<EncounterBundle, ResultSet>(
                cqlOperations.queryAsynchronously("SELECT * FROM encounter WHERE health_id='" + healthId + "';")) {
            @Override
            protected EncounterBundle adapt(ResultSet resultSet) throws ExecutionException {
                Row result = resultSet.one();
                if (null != result) {
                    EncounterBundle bundle = new EncounterBundle();
                    bundle.setHealthId(result.getString("patient_id"));
                    bundle.setEncounterId(result.getString("encounter_id"));
                    bundle.setContent(result.getString("content"));
                    return bundle;
                } else {
                    return null;
                }
            }
        };
    }
}
