package org.freeshr.infrastructure.persistence;

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
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.FileUtil.asString;
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
        encounterRepository.save(createEncounterBundle("e-0", "h100"));
        encounterRepository.save(createEncounterBundle("e-1", "h100"));
        encounterRepository.save(createEncounterBundle("e-2", "h100"));

        Thread.sleep(1000);

        List<EncounterBundle> encounters = encounterRepository.findAll("h100").get();
        EncounterBundle encounter = encounters.get(0);

        assertEquals(3, encounters.size());
        assertThat(encounter.getDate(), is(notNullValue()));
        assertThat(encounter.getEncounterContent().toString(), is(content()));
    }


    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter");
    }

    private EncounterBundle createEncounterBundle(String encounterId, String healthId) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setEncounterId(encounterId);
        bundle.setHealthId(healthId);
        bundle.setEncounterContent(asString("jsons/encounters/valid.json"));
        return bundle;
    }


    private String content() {
        return asString("jsons/encounters/valid.json");
    }
}