package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
import org.joda.time.DateTime;
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

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

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
        DateTime today = new DateTime(2015,02,01,0,0);
        Date monthAfter = today.plusMonths(1).toDate();
        Date twoMonthsAfter = today.plusMonths(2).toDate();
        Date updatedSinceYesterday = today.minusDays(1).toDate();
        encounterRepository.save(createEncounterBundle("e-1", healthId, today.toDate(), "facilityId"), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-2", healthId, monthAfter, "facilityId"), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-3", healthId, twoMonthsAfter, "facilityId"), patient).toBlocking().first();

        List<EncounterBundle> encounterBundles = encounterRepository.findEncountersForPatient(healthId,
                updatedSinceYesterday, 200).toBlocking().single();
        assertEquals(3, encounterBundles.size());

        assertEncounter(encounterBundles, "e-1", today.toDate());
        assertEncounter(encounterBundles, "e-2", monthAfter);
        assertEncounter(encounterBundles, "e-3", twoMonthsAfter);

        Date updatedAfterThreeMonths = today.plusMonths(3).toDate();
        encounterBundles = encounterRepository.findEncountersForPatient(healthId, updatedAfterThreeMonths,
                200).toBlocking().single();
        assertEquals("Should not have returned any encounter as updatedSince is after existing encounter dates", 0,
                encounterBundles.size());
    }

    @Test
    public void shouldFetchEncounterByAddressOnly() throws InterruptedException, ExecutionException, ParseException {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        DateTime today = new DateTime(2015,02,01,0,0);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        Date e1ReceivedDate = today.plusDays(1).toDate();
        Date e2ReceivedDate = today.plusDays(2).toDate();
        encounterRepository.save(createEncounterBundle("e-11", healthId, e1ReceivedDate, "facilityId"), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-12", healthId, e2ReceivedDate, "facilityId"), patient).toBlocking().first();

        List<EncounterBundle> encountersForCatchment = encounterRepository.
                findEncountersForCatchment(new Catchment("0102"), today.toDate(), 10).toBlocking().first();
        assertEquals(2, encountersForCatchment.size());
        assertEncounter(encountersForCatchment, "e-11", e1ReceivedDate);
        assertEncounter(encountersForCatchment, "e-12", e2ReceivedDate);
    }

    @Test
    public void shouldFetchEncounterById() throws ExecutionException, InterruptedException {
        String facilityId = "facilityId";
        Date encounterRecievedTime = new Date();
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        encounterRepository.save(createEncounterBundle("e-0", healthId, encounterRecievedTime, facilityId), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-2", healthId, encounterRecievedTime, facilityId), patient).toBlocking().first();
        EncounterBundle savedEncounter = encounterRepository.findEncounterById("e-0").toBlocking().first();

        assertEquals("e-0", savedEncounter.getEncounterId());
        assertEquals(healthId, savedEncounter.getHealthId());
        assertEquals(encounterRecievedTime, savedEncounter.getReceivedDate());
        assertEquals(encounterRecievedTime, savedEncounter.getUpdatedDate());
        assertEquals(facilityId, savedEncounter.getCreatedBy().getFacilityId());
        assertNull(savedEncounter.getUpdatedBy().getProviderId());
    }

    @Test
    public void shouldSaveEncounterReceivedDateFromTheBundle() throws Exception {
        String facilityId = "facilityId";
        String encounterId = "e-111";
        Date encounterRecievedDate = new Date();
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        encounterRepository.save(createEncounterBundle(encounterId, healthId, encounterRecievedDate, facilityId), patient).toBlocking().first();

        Select selectEncounterQuery = QueryBuilder
                .select("encounter_id", "received_at")
                .from("encounter")
                .where(eq("encounter_id", encounterId))
                .limit(1);
        ResultSet resultSet = cqlOperations.query(selectEncounterQuery);
        assertFalse(resultSet.isExhausted());
        assertEquals(encounterRecievedDate.getTime(), TimeUuidUtil.getTimeFromUUID(resultSet.one().getUUID("received_at")));

        Select selectEncByPatientQuery = QueryBuilder
                .select("encounter_id", "created_at")
                .from("enc_by_patient")
                .where(eq("health_id", healthId))
                .limit(1);
        resultSet = cqlOperations.query(selectEncByPatientQuery);
        assertFalse(resultSet.isExhausted());
        assertEquals(encounterRecievedDate.getTime(), TimeUuidUtil.getTimeFromUUID(resultSet.one().getUUID("created_at")));

        Select selectEncByCatchmentQuery = QueryBuilder
                .select("encounter_id", "created_at")
                .from("enc_by_catchment")
                .where(eq("division_id", "01"))
                .and(eq("district_id", "0102"))
                .and(eq("year", DateUtil.getYearOf(encounterRecievedDate)))
                .limit(1);
        resultSet = cqlOperations.query(selectEncByCatchmentQuery);
        assertFalse(resultSet.isExhausted());
        assertEquals(encounterRecievedDate, TimeUuidUtil.getDateFromUUID(resultSet.one().getUUID("created_at")));
    }

    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter");
        cqlOperations.execute("truncate enc_by_catchment");
        cqlOperations.execute("truncate enc_by_patient");
    }

    private void assertEncounter(List<EncounterBundle> encounterBundles, String encounterId, Date receivedDate) {
        EncounterBundle encounter1 = getEncounterById(encounterBundles, encounterId);
        assertThat(encounter1.getReceivedDate(), is(receivedDate));
        assertThat(encounter1.getEncounterContent().toString(), is(content()));
    }

    private EncounterBundle getEncounterById(List<EncounterBundle> encounterBundles, final String encounterId) {
        return CollectionUtils.find(encounterBundles, new CollectionUtils.Fn<EncounterBundle, Boolean>() {
            @Override
            public Boolean call(EncounterBundle encounterBundle) {
                return encounterBundle.getEncounterId().equals(encounterId);
            }
        });
    }

    private String generateHealthId() {
        return java.util.UUID.randomUUID().toString();
    }

    private EncounterBundle createEncounterBundle(String encounterId, String healthId, Date receivedDate, String facilityId) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setEncounterId(encounterId);
        bundle.setHealthId(healthId);
        bundle.setEncounterConfidentiality(Confidentiality.Normal);
        bundle.setPatientConfidentiality(Confidentiality.Normal);
        bundle.setEncounterContent(asString("jsons/encounters/valid.json"));
        bundle.setReceivedDate(receivedDate);
        bundle.setUpdatedDate(receivedDate);
        bundle.setCreatedBy(new Requester(facilityId, null));
        bundle.setUpdatedBy(new Requester(facilityId, null));
        return bundle;
    }

    private String content() {
        return asString("jsons/encounters/valid.json");
    }
}