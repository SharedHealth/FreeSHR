package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.events.EncounterEvent;
import org.freeshr.interfaces.encounter.ws.APIIntegrationTestBase;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static ch.lambdaj.Lambda.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class EncounterRepositoryIntegrationTest extends APIIntegrationTestBase{

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
        encounterRepository.save(createEncounterBundle("e-1", healthId, Confidentiality.Normal, Confidentiality.Normal, asString("jsons/encounters/valid.json"),  new Requester("facilityId", null), today.toDate()), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-2", healthId, Confidentiality.Normal, Confidentiality.Normal, asString("jsons/encounters/valid.json"),  new Requester("facilityId", null), monthAfter), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-3", healthId, Confidentiality.Normal, Confidentiality.Normal, asString("jsons/encounters/valid.json"),  new Requester("facilityId", null), twoMonthsAfter), patient).toBlocking().first();

        List<EncounterEvent> encounterBundles = encounterRepository.findEncounterFeedForPatient(healthId,
                updatedSinceYesterday, 200).toBlocking().single();
        assertEquals(3, encounterBundles.size());

        assertEncounter(encounterBundles, "e-1", today.toDate());
        assertEncounter(encounterBundles, "e-2", monthAfter);
        assertEncounter(encounterBundles, "e-3", twoMonthsAfter);

        Date updatedAfterThreeMonths = today.plusMonths(3).toDate();
        encounterBundles = encounterRepository.findEncounterFeedForPatient(healthId, updatedAfterThreeMonths,
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
        encounterRepository.save(createEncounterBundle("e-11", healthId, Confidentiality.Normal, Confidentiality.Normal,  asString("jsons/encounters/valid.json"), new Requester("facilityId", null), e1ReceivedDate), patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-12", healthId, Confidentiality.Normal, Confidentiality.Normal,  asString("jsons/encounters/valid.json"), new Requester("facilityId", null), e2ReceivedDate), patient).toBlocking().first();

        List<EncounterEvent> encountersForCatchment = encounterRepository.
                findEncounterFeedForCatchment(new Catchment("0102"), today.toDate(), 10).toBlocking().first();
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

        EncounterBundle encounterBundle1 = createEncounterBundle("e-0", healthId, Confidentiality.Normal, Confidentiality.Normal, asString("jsons/encounters/valid.json"), new Requester(facilityId, null), encounterRecievedTime);
        encounterBundle1.setContentVersion(3);
        encounterRepository.save(encounterBundle1, patient).toBlocking().first();
        encounterRepository.save(createEncounterBundle("e-2", healthId, Confidentiality.Normal, Confidentiality.Normal, asString("jsons/encounters/valid.json"), new Requester(facilityId, null), encounterRecievedTime), patient).toBlocking().first();
        EncounterBundle savedEncounter = encounterRepository.findEncounterById("e-0").toBlocking().first();

        assertEquals("e-0", savedEncounter.getEncounterId());
        assertEquals(healthId, savedEncounter.getHealthId());
        assertEquals(encounterRecievedTime, savedEncounter.getReceivedAt());
        assertEquals(encounterRecievedTime, savedEncounter.getUpdatedAt());
        assertEquals(facilityId, savedEncounter.getCreatedBy().getFacilityId());
        assertEquals(3, savedEncounter.getContentVersion());
        assertNull(savedEncounter.getUpdatedBy().getProviderId());
    }

    @Test
    public void shouldSaveEncounterReceivedDateFromTheBundle() throws Exception {
        String facilityId = "facilityId";
        String encounterId = "e-111";
        Date encounterRecievedAt = new Date();
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        encounterRepository.save(createEncounterBundle(encounterId, healthId, Confidentiality.Normal, Confidentiality.Normal, asString("jsons/encounters/valid.json"), new Requester(facilityId, null), encounterRecievedAt), patient).toBlocking().first();

        Select selectEncounterQuery = QueryBuilder
                .select("encounter_id", "received_at")
                .from("encounter")
                .where(eq("encounter_id", encounterId))
                .limit(1);
        ResultSet resultSet = cqlOperations.query(selectEncounterQuery);
        assertFalse(resultSet.isExhausted());
        assertEquals(encounterRecievedAt.getTime(), TimeUuidUtil.getTimeFromUUID(resultSet.one().getUUID("received_at")));

        Select selectEncByPatientQuery = QueryBuilder
                .select("encounter_id", "created_at")
                .from("enc_by_patient")
                .where(eq("health_id", healthId))
                .limit(1);
        resultSet = cqlOperations.query(selectEncByPatientQuery);
        assertFalse(resultSet.isExhausted());
        assertEquals(encounterRecievedAt.getTime(), TimeUuidUtil.getTimeFromUUID(resultSet.one().getUUID("created_at")));

        Select selectEncByCatchmentQuery = QueryBuilder
                .select("encounter_id", "created_at")
                .from("enc_by_catchment")
                .where(eq("division_id", "01"))
                .and(eq("district_id", "0102"))
                .and(eq("year", DateUtil.getYearOf(encounterRecievedAt)))
                .limit(1);
        resultSet = cqlOperations.query(selectEncByCatchmentQuery);
        assertFalse(resultSet.isExhausted());
        assertEquals(encounterRecievedAt, TimeUuidUtil.getDateFromUUID(resultSet.one().getUUID("created_at")));
    }

    @Test
    public void shouldUpdateEncounter() throws Exception {
        String encounterId = "e-111";
        Date encounterRecievedAt = new Date();
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        Requester createdBy = new Requester("facilityId", null);
        String encounterContent = asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml");
        EncounterBundle existingEncounterBundle = createEncounterBundle(encounterId, healthId, Confidentiality.Normal,
                Confidentiality.Normal, encounterContent, createdBy, encounterRecievedAt);
        encounterRepository.save(existingEncounterBundle, patient).toBlocking().first();

        Date updatedAt = new Date();
        Requester updatedBy = new Requester("facilityId1", null);
        encounterRepository.updateEncounter(createUpdateEncounterBundle(encounterId, healthId, Confidentiality.Normal,
                        encounterContent, updatedBy, updatedAt),
        existingEncounterBundle, patient).toBlocking().first();

        Select selectEncounterQuery = QueryBuilder
                .select()
                .all()
                .from("encounter")
                .where(eq("encounter_id", encounterId))
                .limit(10);
        ResultSet resultSet = cqlOperations.query(selectEncounterQuery);
        assertFalse(resultSet.isExhausted());
        Row updatedEncounterRow = resultSet.one();
        assertEquals(encounterRecievedAt.getTime(), TimeUuidUtil.getTimeFromUUID(updatedEncounterRow.getUUID("received_at")));
        assertEquals(updatedAt.getTime(), TimeUuidUtil.getTimeFromUUID(updatedEncounterRow.getUUID("updated_at")));
        assertEquals(createdBy, new ObjectMapper().readValue(updatedEncounterRow.getString("created_by"), Requester.class));
        assertEquals(updatedBy, new ObjectMapper().readValue(updatedEncounterRow.getString("updated_by"), Requester.class));
        assertEquals(encounterContent, updatedEncounterRow.getString("content_v2"));
        assertTrue(resultSet.isExhausted());

        Select selectEncByPatientQuery = QueryBuilder
                .select("encounter_id", "created_at")
                .from("enc_by_patient")
                .where(eq("health_id", healthId))
                .limit(10);
        List<Row> encByPatientRows = cqlOperations.query(selectEncByPatientQuery).all();
        assertEquals(2, encByPatientRows.size());
        assertEquals(encounterRecievedAt.getTime(), TimeUuidUtil.getTimeFromUUID(encByPatientRows.get(0).getUUID("created_at")));
        assertEquals(encounterId, encByPatientRows.get(0).getString("encounter_id"));
        assertEquals(updatedAt.getTime(), TimeUuidUtil.getTimeFromUUID(encByPatientRows.get(1).getUUID("created_at")));
        assertEquals(encounterId, encByPatientRows.get(1).getString("encounter_id"));

        Select selectEncByCatchmentQuery = QueryBuilder
                .select("encounter_id", "created_at")
                .from("enc_by_catchment")
                .where(eq("division_id", "01"))
                .and(eq("district_id", "0102"))
                .and(eq("year", DateUtil.getYearOf(encounterRecievedAt)))
                .limit(10);
        List<Row> encByCatchmentRows = cqlOperations.query(selectEncByCatchmentQuery).all();
        assertEquals(2, encByCatchmentRows.size());
        assertEquals(encounterRecievedAt.getTime(), TimeUuidUtil.getTimeFromUUID(encByCatchmentRows.get(0).getUUID("created_at")));
        assertEquals(encounterId, encByCatchmentRows.get(0).getString("encounter_id"));
        assertEquals(updatedAt.getTime(), TimeUuidUtil.getTimeFromUUID(encByCatchmentRows.get(1).getUUID("created_at")));
        assertEquals(encounterId, encByCatchmentRows.get(1).getString("encounter_id"));

        Select selectEncHistoryQuery = QueryBuilder
                .select()
                .all()
                .from("enc_history")
                .where(eq("encounter_id", encounterId))
                .limit(10);
        List<Row> encHistoryRows = cqlOperations.query(selectEncHistoryQuery).all();
        assertEquals(1, encHistoryRows.size());
        Row encounterHistoryRow = encHistoryRows.get(0);
        assertEquals(encounterRecievedAt.getTime(), TimeUuidUtil.getTimeFromUUID(encounterHistoryRow.getUUID("encounter_updated_at")));
        assertEquals("v2", encounterHistoryRow.getString("content_format"));
    }

    @Test
    public void shouldFetchEncountersForPatientInTheOrderOfEvents() throws Exception {
        String encounterOne = "e-1";
        String encounterTwo = "e-2";
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        Date jan1st0930 = new DateTime(2015,01,01,9,30).toDate();
        Date jan1st0940 = new DateTime(2015,01,01,9,40).toDate();
        Date jan1st0945 = new DateTime(2015,02,01,9,45).toDate();

        Requester createdBy = new Requester("facilityId", null);
        EncounterBundle existingEncounterBundle = createEncounterBundle(encounterOne, healthId, Confidentiality.Normal,
                Confidentiality.Normal, asString("jsons/encounters/valid.json"), createdBy, jan1st0930);
        encounterRepository.save(existingEncounterBundle, patient).toBlocking().first();

        EncounterBundle secondEncounter = createEncounterBundle(encounterTwo, healthId, Confidentiality.Normal,
                Confidentiality.Normal, asString("jsons/encounters/valid.json"), createdBy, jan1st0940);
        encounterRepository.save(secondEncounter, patient).toBlocking().first();

        Requester updatedBy = new Requester("facilityId1", null);
        encounterRepository.updateEncounter(createUpdateEncounterBundle(encounterOne, healthId, Confidentiality.Normal,
                        asString("jsons/encounters/valid.json"), updatedBy, jan1st0945),
                existingEncounterBundle, patient).toBlocking().first();

        List<EncounterEvent> encounterEventsForPatient = encounterRepository.findEncounterFeedForPatient(healthId, null, 5).toBlocking().first();

        assertEquals(3, encounterEventsForPatient.size());
        assertEquals("e-1", encounterEventsForPatient.get(0).getEncounterId());
        assertEquals(jan1st0930,encounterEventsForPatient.get(0).getUpdatedAt());

        assertEquals("e-2", encounterEventsForPatient.get(1).getEncounterId());
        assertEquals(jan1st0940,encounterEventsForPatient.get(1).getUpdatedAt());

        assertEquals("e-1", encounterEventsForPatient.get(2).getEncounterId());
        assertEquals(jan1st0945,encounterEventsForPatient.get(2).getUpdatedAt());


    }

    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter");
        cqlOperations.execute("truncate enc_by_catchment");
        cqlOperations.execute("truncate enc_by_patient");
        cqlOperations.execute("truncate enc_history");
    }

    private void assertEncounter(List<EncounterEvent> encounterEvents, String encounterId, Date receivedDate) {
        EncounterEvent encounterEvent = selectFirst(encounterEvents, having(on(EncounterEvent.class).getEncounterId(), Matchers.equalTo(encounterId)));
        assertThat(encounterEvent.getEncounterReceivedAt(), is(receivedDate));
        assertThat(encounterEvent.getContent(), is(content()));
    }

    private String content() {
        return asString("jsons/encounters/valid.json");
    }
}