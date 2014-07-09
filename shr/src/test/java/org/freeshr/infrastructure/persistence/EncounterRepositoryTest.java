package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.Row;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class EncounterRepositoryTest {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    CqlOperations cqlOperations;


    @Test
    public void shouldCreateAndFindAllEncounters() throws InterruptedException {
        encounterRepository.save(createEncounterBundle("0"));
        encounterRepository.save(createEncounterBundle("1"));
        encounterRepository.save(createEncounterBundle("2"));

        final List<Row> resultSet = cqlOperations.query("SELECT * FROM freeshr.encounter;").all();
        final int size = resultSet.size();
        assertEquals(3, size);

        for (int i = 0; i < size; i++) {
            Row result = resultSet.get(i);
            assertEquals("e100-" + i, result.getString("encounter_id"));
            assertEquals("h100", result.getString("health_id"));
            assertEquals("2012-01-04T09:10:14Z", result.getString("date"));
            assertEquals("helloworld-" + i, result.getString("content"));
        }
    }

    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter");
    }

    private EncounterBundle createEncounterBundle(String counter) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setEncounterId("e100-" + counter);
        bundle.setHealthId("h100");
        bundle.setDate("2012-01-04T09:10:14Z");
        bundle.setContent("helloworld-" + counter);
        return bundle;
    }
}