package org.freeshr.infrastructure.persistence;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.Confidentiality;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


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
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));
        Date received_date = new Date();
        encounterRepository.save(createEncounterBundle("e-0", healthId), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-1", healthId), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-2", healthId), patient).toBlocking().first();

        List<EncounterBundle> encounterBundles = encounterRepository.findEncountersForPatient(healthId,
                received_date, 200).toBlocking().single();
        EncounterBundle encounter = encounterBundles.get(0);
        assertEquals(3, encounterBundles.size());
        assertThat(encounter.getReceivedDate(), is(notNullValue()));
        assertThat(encounter.getEncounterContent().toString(), is(content()));

        encounterBundles = encounterRepository.findEncountersForPatient(healthId, new Date(),
                200).toBlocking().single();
        assertEquals("Should not have returned any encounter as updatedSince is after existing encounter dates", 0,
                encounterBundles.size());
    }


    private String generateHealthId() {
        return java.util.UUID.randomUUID().toString();
    }

    private void saveEncounterWithDate(String encounterId, String healthId, String date) {
        //TODO why can't we call encounterRepository.save()??????
        cqlOperations.execute("INSERT INTO encounter (encounter_id, health_id, date, content,division_id, " +
                "district_id, upazilla_id, city_corporation_id,ward_id) VALUES ( '"
                + encounterId + "','" + healthId + "','" + date + "',"
                + "'some-content','10','1020','102030','10203040','1020304050');");

    }

    @Test
    public void shouldFetchEncounterByAddressOnly() throws InterruptedException, ExecutionException, ParseException {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        Date date = new Date();
        patient.setAddress(new Address("01", "02", "03", "04", "05"));
        encounterRepository.save(createEncounterBundle("e-0", healthId), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-2", healthId), patient).toBlocking().first();
        List<EncounterBundle> encountersForCatchment = encounterRepository.
                findEncountersForCatchment(new Catchment("0102"), date, 10).toBlocking().first();
        System.out.println(encountersForCatchment);
        assertEquals(2, encountersForCatchment.size());
        EncounterBundle encounter = encountersForCatchment.get(0);
        assertThat(encounter.getReceivedDate(), is(notNullValue()));
    }

    @Test
    public void shouldFetchEncounterById() throws ExecutionException, InterruptedException {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        Date date = new Date();
        patient.setAddress(new Address("01", "02", "03", "04", "05"));
        encounterRepository.save(createEncounterBundle("e-0", healthId), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-2", healthId), patient).toBlocking().first();
        Observable<EncounterBundle> encounterById = encounterRepository.findEncounterById("e-0");
        assertEquals("e-0", encounterById.toBlocking().first().getEncounterId());

    }

    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter");
    }

    private EncounterBundle createEncounterBundle(String encounterId, String healthId) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setEncounterId(encounterId);
        bundle.setHealthId(healthId);
        bundle.setEncounterConfidentiality(Confidentiality.Normal);
        bundle.setPatientConfidentiality(Confidentiality.Normal);
        bundle.setEncounterContent(asString("jsons/encounters/valid.json"));
        return bundle;
    }


    private String content() {
        return asString("jsons/encounters/valid.json");
    }
}