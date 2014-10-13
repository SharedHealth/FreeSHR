package org.freeshr.infrastructure.persistence;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class PatientRepositoryIntegrationTest {

    private final String healthId = "testHealthId";

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    private CqlOperations cqlTemplate;

    @Test
    public void shouldFindPatientWithMatchingHealthId() throws ExecutionException, InterruptedException {
        patientRepository.save(patient(healthId));
        Patient patient = patientRepository.find(healthId);
        assertNotNull(patient);
        assertThat(patient, is(patient(healthId)));
        assertThat(patient.getAddress(), is(address()));
    }

    private Patient patient(String healthId) {
        Patient patient = new Patient();
        patient.setHealthId(healthId);
        patient.setGender("1");
        patient.setAddress(address());
        return patient;
    }

    private Address address() {
        Address address = new Address();
        address.setDistrict("district");
        address.setDivision("division");
        address.setLine("line");
        address.setUpazilla("upazilla");
        address.setWard("union");
        address.setCityCorporation("cityCorporation");
        return address;
    }

    @Test
    public void shouldNotFindPatientWithoutMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNull(patientRepository.find(healthId + "invalid"));
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate patient");
    }

}