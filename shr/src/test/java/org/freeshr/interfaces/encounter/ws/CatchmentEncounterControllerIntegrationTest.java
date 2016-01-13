package org.freeshr.interfaces.encounter.ws;

import net.sf.ehcache.CacheManager;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.Confidentiality.Normal;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.HttpUtil.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997","ENCOUNTER_FETCH_LIMIT=20"})
public class CatchmentEncounterControllerIntegrationTest extends APIIntegrationTestBase {
    private final String validClientId = "6";
    private final String validEmail = "some@thoughtworks.com";
    private final String validAccessToken = "2361e0a8-f352-4155-8415-32adfb8c2472";

    @Autowired
    SHRProperties properties;

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailsWithAllRoles.json"))));
    }

    @After
    public void tearDown() throws Exception {
        CacheManager.getInstance().clearAll();
    }

    @Test
    public void shouldGetEncountersForCatchment() throws Exception {
        Patient patient1 = new Patient();
        String healthId1 = generateHealthId();
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("30", "26", "18", "01", "02"));
        final Requester createdBy = new Requester("facilityId", "providerId");
        createEncounter(createEncounterBundle("e-0100-" + healthId1, healthId1, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient1);
        createEncounter(createEncounterBundle("e-1100-" + healthId1, healthId1, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient1);
        createEncounter(createEncounterBundle("e-2100-" + healthId1, healthId1, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient1);

        Patient patient2 = new Patient();
        String healthId2 = generateHealthId();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("30", "26", "18", "02", "02"));
        createEncounter(createEncounterBundle("e-0200-" + healthId2, healthId2, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient2);
        createEncounter(createEncounterBundle("e-1200-" + healthId2, healthId2, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient2);
        createEncounter(createEncounterBundle("e-2200-" + healthId2, healthId2, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient2);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());

        Facility facility = new Facility("10000069", "facility1", "Main hospital", "3026, 30261801",
                new Address("30", "26", "18", null, null));
        createFacility(facility);

        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + "3026" + "/encounters?updatedSince=" + today)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(6)));

        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + "30261801" + "/encounters?updatedSince=" + today)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(3)));
    }

    @Test
    public void shouldFetchMaximumNumberOfEncountersAsDefinedByLimitForCatchmentUpdatedSinceGivenDate() throws Exception {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("30", "26", "18", "01", "02"));
        final Requester createdBy = new Requester("facilityId", "providerId");

        Date date = new Date();

        int encounterFetchLimit = properties.getEncounterFetchLimit();
        List<Date> encounterDates = getTimeInstances(date, encounterFetchLimit + 30);
        for(int i=0; i<50; i++){
            createEncounter(createEncounterBundle("E"+ i, healthId, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, encounterDates.get(i)), patient);
        }

        List<String> expectedEventIds = getUuidsForDates(encounterDates.subList(0,encounterFetchLimit-1));

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/catchments/3026/encounters?updatedSince=%s",new SimpleDateFormat("yyyy-MM-dd").format(date)))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(encounterFetchLimit)))
                .andExpect(request().asyncResult(hasEvents(expectedEventIds)));
    }

    @Test
    public void shouldFetchEncountersForCatchmentUpdatedSinceGivenDate() throws Exception {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("30", "26", "18", "01", "02"));
        final Requester createdBy = new Requester("facilityId", "providerId");

        Date date = new Date();

        int sizeLessThanFetchLimit = properties.getEncounterFetchLimit() - 10;
        List<Date> encounterDates = getTimeInstances(date, sizeLessThanFetchLimit);
        for(int i=0; i< sizeLessThanFetchLimit; i++){
            createEncounter(createEncounterBundle("E"+ i, healthId, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, encounterDates.get(i)), patient);
        }

        List<String> expectedEventIds = getUuidsForDates(encounterDates.subList(0, sizeLessThanFetchLimit -1));

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/catchments/3026/encounters?updatedSince=%s",new SimpleDateFormat("yyyy-MM-dd").format(date)))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(sizeLessThanFetchLimit)))
                .andExpect(request().asyncResult(hasEvents(expectedEventIds)));
    }

    @Test
    public void shouldFetchMaximumNumberOfEncountersAsDefinedByLimitSinceLastReadMarker() throws Exception {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("30", "26", "18", "01", "02"));
        final Requester createdBy = new Requester("facilityId", "providerId");

        Date date = new Date();

        List<Date> encounterDates = getTimeInstances(date, 50);
        for(int i=0; i<50; i++){
            createEncounter(createEncounterBundle("E"+ i, healthId, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, encounterDates.get(i)), patient);
        }

        String marker25 = TimeUuidUtil.uuidForDate(encounterDates.get(24)).toString();
        List<String> markersFrom25till45 = getUuidsForDates(encounterDates.subList(24,44));

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/catchments/3026/encounters?updatedSince=%s&lastMarker=%s", new SimpleDateFormat("yyyy-MM-dd").format(date), marker25))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(20)))
                .andExpect(request().asyncResult(hasEvents(markersFrom25till45)));

    }

    @Test
    public void shouldFetchAllEncountersSinceLastReadMarker() throws Exception {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("30", "26", "18", "01", "02"));
        final Requester createdBy = new Requester("facilityId", "providerId");

        Date date = new Date();

        List<Date> encounterDates = getTimeInstances(date, 50);
        for(int i=0; i< 50; i++){
            createEncounter(createEncounterBundle("E"+ i, healthId, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, encounterDates.get(i)), patient);
        }

        String marker40 = TimeUuidUtil.uuidForDate(encounterDates.get(39)).toString();
        List<String> markersFrom40till50 = getUuidsForDates(encounterDates.subList(39,49));

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/catchments/3026/encounters?updatedSince=%s&lastMarker=%s", new SimpleDateFormat("yyyy-MM-dd").format(date), marker40))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(11)))
                .andExpect(request().asyncResult(hasEvents(markersFrom40till50)));

    }

    private List<String> getUuidsForDates(List<Date> dates) {
        List<String> uuids = new ArrayList<>();
        for (Date date : dates) {
            uuids.add(TimeUuidUtil.uuidForDate(date).toString());
        }
        return uuids;
    }

    private List<Date> getTimeInstances(Date startingFrom, int size) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startingFrom);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);

        for (int i = 0; i < size; i++) {
            calendar.set(Calendar.SECOND, i);
            dates.add(calendar.getTime());
        }
        return dates;
    }
}