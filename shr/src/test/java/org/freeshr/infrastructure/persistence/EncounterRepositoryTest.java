package org.freeshr.infrastructure.persistence;

import com.google.gson.Gson;
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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class EncounterRepositoryTest {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    CqlOperations cqlOperations;

    @Test
    public void shouldFetchAllEncounters() throws InterruptedException, ExecutionException {
        encounterRepository.save(createEncounterBundle("e-0"));
        encounterRepository.save(createEncounterBundle("e-1"));
        encounterRepository.save(createEncounterBundle("e-2"));

        List<EncounterBundle> encounters = encounterRepository.findAll("h100").get();

        assertEquals(3, encounters.size());
    }

    @Test
    public void shouldSaveAndRetrieveAnEncounter() throws ExecutionException, InterruptedException {
        encounterRepository.save(createEncounterBundle("test-encounter"));

        EncounterBundle encounter = encounterRepository.findAll("h100").get().get(0);

        assertEquals("test-encounter", encounter.getEncounterId());
        assertThat(encounter.getDate(), is(notNullValue()));
        assertThat(encounter.getContent().toString(), is(patientDetails()));
    }

    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter");
    }

    private EncounterBundle createEncounterBundle(String encounterId) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setEncounterId(encounterId);
        bundle.setHealthId("h100");
        bundle.setContent(patientDetails());
        return bundle;
    }

    private String patientDetails() {
        HashMap<String, Object> content = new HashMap<String, Object>();
        HashMap<String, String> patient = new HashMap<String, String>();
        patient.put("gender", "Male");
        patient.put("address", "test address");
        patient.put("blood_group", "a positive");
        content.put("patient", patient);
        return new Gson().toJson(content);
    }
}