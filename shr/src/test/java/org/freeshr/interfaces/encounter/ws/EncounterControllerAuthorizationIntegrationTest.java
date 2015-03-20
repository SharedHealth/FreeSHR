package org.freeshr.interfaces.encounter.ws;

import com.google.common.base.Charsets;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.Confidentiality.Normal;
import static org.freeshr.utils.Confidentiality.VeryRestricted;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.HttpUtil.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "MCI_SERVER_URL=http://localhost:9997")
public class EncounterControllerAuthorizationIntegrationTest extends APIIntegrationTestBase {
    private static final String VALID_HEALTH_ID = "5893922485019082753";
    private static final String ENCOUNTER_ID = "dfbc9b30-ceef-473e-9q22-4ee31qfceqdd";
    private static final String PATIENT_CATCHMENT = "0102";
    private static final String DATASENSE_REGISTERED_DIVISION = "01";
    private static final String DATASENSE_REGISTERED_DISTRICT = "02";

    @Autowired
    SHRProperties properties;

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/v1/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient_not_confidential.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/eddb01eb-61fc-4f9e-aca5-e44193509f35"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept.json"))));
    }

    @Test
    public void facilityShouldPostEncounter() throws Exception {
        final String validClientId = "18548";
        final String validEmail = "facility@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForFacility.json"))));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_to_save.xml")))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));
    }

    @Test
    public void providerShouldPostEncounter() throws Exception {
        final String validClientId = "18556";
        final String validEmail = "provider@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForProvider.json"))));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_to_save.xml")))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));
    }

    @Test
    public void datasenseShouldNotPostEncounter() throws Exception {
        final String validClientId = "18552";
        final String validEmail = "datasense@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForDatasense.json"))));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_to_save.xml")))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void patientShouldNotPostEncounter() throws Exception {
        final String validClientId = "18558";
        final String validEmail = "patient@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForPatient.json"))));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_to_save.xml")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void facilityShouldGetNonConfidentialEncounter() throws Exception {
        final String validClientId = "18548";
        final String validEmail = "facility@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createNonConfidentialEncounter(VALID_HEALTH_ID, DATASENSE_REGISTERED_DIVISION, DATASENSE_REGISTERED_DISTRICT);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForFacility.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void facilityShouldNotGetConfidentialEncounter() throws Exception {
        final String validClientId = "18548";
        final String validEmail = "facility@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createConfidentialEncounter(VALID_HEALTH_ID);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForFacility.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(0)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void providerShouldGetNonConfidentialEncounter() throws Exception {
        final String validClientId = "18556";
        final String validEmail = "provider@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createNonConfidentialEncounter(VALID_HEALTH_ID, DATASENSE_REGISTERED_DIVISION, DATASENSE_REGISTERED_DISTRICT);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForProvider.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void providerShouldNotGetConfidentialEncounter() throws Exception {
        final String validClientId = "18556";
        final String validEmail = "provider@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createConfidentialEncounter(VALID_HEALTH_ID);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForProvider.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(0)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void datasenseShouldGetConfidentialEncounter() throws Exception {
        final String validClientId = "18552";
        final String validEmail = "datasense@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createConfidentialEncounter(VALID_HEALTH_ID);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForDatasense.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void datasenseShouldGetNonConfidentialEncounter() throws Exception {
        final String validClientId = "18552";
        final String validEmail = "datasense@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createNonConfidentialEncounter(VALID_HEALTH_ID, DATASENSE_REGISTERED_DIVISION, DATASENSE_REGISTERED_DISTRICT);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForDatasense.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void patientShouldGetNonConfidentialEncounter() throws Exception {
        final String validClientId = "18558";
        final String validEmail = "patient@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createNonConfidentialEncounter(VALID_HEALTH_ID, DATASENSE_REGISTERED_DIVISION, DATASENSE_REGISTERED_DISTRICT);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForPatient.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void patientShouldGetConfidentialEncounter() throws Exception {
        final String validClientId = "18558";
        final String validEmail = "patient@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createConfidentialEncounter(VALID_HEALTH_ID);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForPatient.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(1)));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void patientShouldNotAccessEncountersOfOtherPatient() throws Exception {
        final String validClientId = "18558";
        final String validEmail = "patient@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";
        String otherHealthId = "58939224850";

        createConfidentialEncounter(otherHealthId);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForPatient.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + otherHealthId + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(isForbidden()));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + otherHealthId + "/encounters/" + ENCOUNTER_ID)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(isForbidden()));
    }

    @Test
    public void datasenseShouldGetEncountersFromRegisteredCatchment() throws Exception {
        final String validClientId = "18552";
        final String validEmail = "datasense@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        createNonConfidentialEncounter(VALID_HEALTH_ID, DATASENSE_REGISTERED_DIVISION, DATASENSE_REGISTERED_DISTRICT);

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForDatasense.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + PATIENT_CATCHMENT + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncountersOfSize(1)));
    }

    @Test
    public void datasenseNotShouldGetEncountersFromNonRegisteredCatchment() throws Exception {
        final String validClientId = "18552";
        final String validEmail = "datasense@gmail.com";
        final String validAccessToken = "40214a6c-e27c-4223-981c-1f837be90f97";

        String otherCatchment = "1020";
        createNonConfidentialEncounter(VALID_HEALTH_ID, "10", "20");

        givenThat(get(urlEqualTo("/token/" + validAccessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/userDetailForDatasense.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + otherCatchment + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(isForbidden()));
    }

    private void createNonConfidentialEncounter(String healthId, String division, String district) throws Exception {
        Patient patient = createPatient(healthId, division, district);

        createEncounter(createEncounterBundle(ENCOUNTER_ID, healthId, Normal, Normal), patient);
    }

    private void createConfidentialEncounter(String healthId) throws Exception {
        Patient patient = createPatient(healthId, DATASENSE_REGISTERED_DIVISION, DATASENSE_REGISTERED_DISTRICT);

        createEncounter(createEncounterBundle(ENCOUNTER_ID, healthId, VeryRestricted, Normal), patient);
    }

    private Patient createPatient(String healthId, String division, String district) {
        Patient patient = new Patient();
        patient.setHealthId(healthId);
        patient.setAddress(new Address(division, district, "03", "04", "05"));
        return patient;
    }
}