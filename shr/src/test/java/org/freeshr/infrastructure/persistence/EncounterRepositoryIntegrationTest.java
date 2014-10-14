package org.freeshr.infrastructure.persistence;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.DateUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class EncounterRepositoryIntegrationTest {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    CqlOperations cqlOperations;

    @Test
    public void shouldFetchEncounterByHealthId() throws InterruptedException, ExecutionException {
        Patient patient1 = new Patient();
        patient1.setHealthId("h100");
        patient1.setAddress(new Address("01", "02", "03", "04", "05"));
        encounterRepository.save(createEncounterBundle("e-0", "h100"), patient1);
        encounterRepository.save(createEncounterBundle("e-1", "h100"), patient1);
        encounterRepository.save(createEncounterBundle("e-2", "h100"), patient1);

        List<EncounterBundle> encounterBundles = encounterRepository.findAll("h100");
        EncounterBundle encounter = encounterBundles.get(0);
        assertEquals(3, encounterBundles.size());
        assertThat(encounter.getDate(), is(notNullValue()));
        assertThat(encounter.getEncounterContent().toString(), is(content()));
    }

    @Test
    public void shouldFetchEncounterByAddressAndDate() throws InterruptedException, ExecutionException, ParseException {
        Patient patient1 = new Patient();
        patient1.setHealthId("h100");
        patient1.setAddress(new Address("01", "02", "03", "04", "05"));
        saveEncounterWithDate("e-0", "h100", "2014-03-13 09:23:31");
        encounterRepository.save(createEncounterBundle("e-2", "h100"), patient1);

        List<EncounterBundle> encounters = encounterRepository.findAllEncountersByCatchment("0102", "district_id", "2011-09-10");
        EncounterBundle encounter = encounters.get(0);
        assertEquals(1, encounters.size());
        assertThat(encounter.getDate(), is(notNullValue()));
        assertThat(encounter.getEncounterId(), is("e-2"));

    }

    private void saveEncounterWithDate(String encounterId, String healthId, String date) {
        cqlOperations.execute("INSERT INTO encounter (encounter_id, health_id, date, content,division_id, " +
                "district_id, upazilla_id, city_corporation_id,ward_id) VALUES ( '"
                + encounterId + "','" + healthId + "','" + date + "',"
                + "'some-content','10','1020','102030','10203040','1020304050');");

    }

    @Test
    public void shouldFetchEncounterByAddressOnly() throws InterruptedException, ExecutionException, ParseException {
        Patient patient1 = new Patient();
        patient1.setHealthId("h100");
        patient1.setAddress(new Address("01", "02", "03", "04", "05"));
        encounterRepository.save(createEncounterBundle("e-0", "h100"), patient1);
        encounterRepository.save(createEncounterBundle("e-2", "h100"), patient1);

        String date = "2014-09-10";
        List<EncounterBundle> encounters = encounterRepository.findAllEncountersByCatchment("0102", "district_id", date);
        EncounterBundle encounter = encounters.get(0);
        assertEquals(2, encounters.size());
        assertThat(encounter.getDate(), is(notNullValue()));
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