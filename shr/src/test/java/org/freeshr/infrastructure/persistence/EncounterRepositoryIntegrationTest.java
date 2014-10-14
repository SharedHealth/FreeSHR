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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


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
        final DateUtil dateUtil = mock(DateUtil.class);
        encounterRepository.setDateUtil(dateUtil);
        when(dateUtil.getCurrentTimeInUTC()).thenReturn("2001-09-11 12:50:32").thenReturn("2014-03-13 09:23:31");
        when(dateUtil.fromUTCDate(any(Date.class))).thenReturn("2014-03-13 09:23:31");

        Patient patient1 = new Patient();
        patient1.setHealthId("h100");
        patient1.setAddress(new Address("01", "02", "03", "04", "05"));
        encounterRepository.save(createEncounterBundle("e-0", "h100"), patient1);
        encounterRepository.save(createEncounterBundle("e-2", "h100"), patient1);

        List<EncounterBundle> encounters = encounterRepository.findAllEncountersByCatchment("0102", "district_id", "2011-09-10");
        EncounterBundle encounter = encounters.get(0);
        assertEquals(1, encounters.size());
        assertThat(encounter.getDate(), is(notNullValue()));
        assertThat(encounter.getEncounterId(), is("e-2"));
        verify(dateUtil, times(1)).fromUTCDate(any(Date.class));

        //hack to fix the build. will not work if this test fails. do not set
        // a mock dateutil into a singleton registry object
        encounterRepository.setDateUtil(new DateUtil());
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