package org.freeshr.shr.patient.repository;

import org.freeshr.shr.config.EnvironmentMock;
import org.freeshr.shr.config.SHRConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = EnvironmentMock.class, classes = SHRConfig.class)
public class AllPatientsTest {

    private final String healthId = "testHealthId";

    @Autowired
    private AllPatients allPatients;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    private CqlOperations cqlTemplate;

    @Before
    public void setup() {
        cqlTemplate.execute("INSERT into patient (health_id) VALUES ('" + healthId + "');");
    }

    @Test
    public void shouldFindPatientWithMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNotNull(allPatients.find(healthId).get());
    }

    @Test
    public void shouldNotFindPatientWithoutMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNull(allPatients.find(healthId + "invalid").get());
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate patient");
    }

}