package org.freeshr.shr.patient.repository;

import org.freeshr.shr.config.SHRConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SHRConfig.class)
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
    public void shouldFindPatientWithMatchingHealthId() {
        assertNotNull(allPatients.find(healthId));
    }

    @Test
    public void shouldNotFindPatientWithoutMatchingHealthId() {
        cqlTemplate.execute("TRUNCATE patient");
    }

}