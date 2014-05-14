package org.freeshr.shr.encounter.repository;

import org.freeshr.shr.config.EnvironmentMock;
import org.freeshr.shr.config.SHRConfig;
import org.freeshr.shr.encounter.model.Encounter;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = EnvironmentMock.class, classes = SHRConfig.class)
public class AllEncountersTest {

    @Autowired
    AllEncounters allEncounters;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    CqlOperations cqlOperations;

    @Test
    public void shouldCreateEncounter() {
        Encounter encounter = new Encounter();
        allEncounters.save(encounter);
        assertEquals(1, cqlOperations.query("SELECT * FROM freeshr.patient;").all().size());
    }

    @After
    public void teardown() {
        cqlOperations.execute("truncate patient");
    }
}