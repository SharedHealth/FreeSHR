package org.freeshr.interfaces.encounter.ws;

import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.Confidentiality.Normal;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.HttpUtil.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@TestPropertySource(properties = "MCI_SERVER_URL=http://localhost:9997")
public class CatchmentEncounterControllerIntegrationTest extends APIIntegrationTestBase {
    private final String validClientId = "6";
    private final String validEmail = "some@thoughtworks.com";
    private final String validAccessToken = "2361e0a8-f352-4155-8415-32adfb8c2472";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailsWithAllRoles.json"))));
    }

    @Test
    public void shouldGetEncountersForCatchment() throws Exception {
        Patient patient1 = new Patient();
        String healthId1 = generateHealthId();
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("30", "26", "18", "01", "02"));
        createEncounter(createEncounterBundle("e-0100-" + healthId1, healthId1, Normal, Normal, new Requester("facilityId", "providerId"), asString("jsons/encounters/valid.json")), patient1);
        createEncounter(createEncounterBundle("e-1100-" + healthId1, healthId1, Normal, Normal, new Requester("facilityId", "providerId"), asString("jsons/encounters/valid.json")), patient1);
        createEncounter(createEncounterBundle("e-2100-" + healthId1, healthId1, Normal, Normal, new Requester("facilityId", "providerId"), asString("jsons/encounters/valid.json")), patient1);

        Patient patient2 = new Patient();
        String healthId2 = generateHealthId();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("30", "26", "18", "02", "02"));
        createEncounter(createEncounterBundle("e-0200-" + healthId2, healthId2, Normal, Normal, new Requester("facilityId", "providerId"), asString("jsons/encounters/valid.json")), patient2);
        createEncounter(createEncounterBundle("e-1200-" + healthId2, healthId2, Normal, Normal, new Requester("facilityId", "providerId"), asString("jsons/encounters/valid.json")), patient2);
        createEncounter(createEncounterBundle("e-2200-" + healthId2, healthId2, Normal, Normal, new Requester("facilityId", "providerId"), asString("jsons/encounters/valid.json")), patient2);

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
                .andExpect(request().asyncResult(hasEncountersOfSize(6)));

        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + "30261801" + "/encounters?updatedSince=" + today)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(hasEncountersOfSize(3)));
    }
}