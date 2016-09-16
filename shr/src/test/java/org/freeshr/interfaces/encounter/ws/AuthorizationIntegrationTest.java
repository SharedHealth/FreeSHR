package org.freeshr.interfaces.encounter.ws;

import com.google.common.base.Charsets;
import net.sf.ehcache.CacheManager;
import org.freeshr.application.fhir.*;
import org.freeshr.application.fhir.Error;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.interfaces.encounter.ws.exceptions.UnProcessableEntity;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.Confidentiality.Normal;
import static org.freeshr.utils.Confidentiality.VeryRestricted;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.HttpUtil.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997", "FACILITY_REGISTRY_URL=http://localhost:9997/facilities/", "PROVIDER_REGISTRY_URL=http://localhost:9997/providers/"})
public class AuthorizationIntegrationTest extends APIIntegrationTestBase {
    private static final String VALID_HEALTH_ID = "98001046534";
    private static final String ENCOUNTER_ID = "dfbc9b30-ceef-473e-9q22-4ee31qfceqdd";
    private static final String PATIENT_CATCHMENT = "0102";
    private static final String DATASENSE_REGISTERED_DIVISION = "01";
    private static final String DATASENSE_REGISTERED_DISTRICT = "02";

    @Autowired
    SHRProperties properties;

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient_not_confidential.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/referenceterms/2f6z9872-4df1-438e-9d72-0a8b161d409b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/ref_term_dengue.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_dengue.json"))));

        givenThat(get(urlEqualTo("/facilities/10000069.json"))
                .withHeader("client_id", matching("18550"))
                .withHeader("X-Auth-Token", matching("c6e6fd3a26313eb250e1019519af33e743808f5bb50428ae5423b8ee278e6fa5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility10000069.json"))));

        givenThat(get(urlEqualTo("/facilities/10019841.json"))
                .withHeader("client_id", matching("18550"))
                .withHeader("X-Auth-Token", matching("c6e6fd3a26313eb250e1019519af33e743808f5bb50428ae5423b8ee278e6fa5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility10019841.json"))));

        givenThat(get(urlEqualTo("/providers/19.json"))
                .willReturn(aResponse()
                        .withStatus(200)));

    }

    @After
    public void tearDown() throws Exception {
        CacheManager.getInstance().clearAll();
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
                .accept(MediaType.APPLICATION_ATOM_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(status().isOk())
                .andExpect(request()
                .asyncResult(new InstanceOf(EncounterResponse.class)));
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
                .accept(MediaType.APPLICATION_ATOM_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml")))
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
                .accept(MediaType.APPLICATION_ATOM_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml")))
                .andExpect(status().isForbidden());
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
                        .withBody(asString("jsons/userDetailForPatient98001046534.json"))));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml")))
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
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)));

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
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)));

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
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)));

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
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)));

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
                        .withBody(asString("jsons/userDetailForPatient98001046534.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)));

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
                        .withBody(asString("jsons/userDetailForPatient98001046534.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + VALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request()
                .asyncResult(hasEncounterEventsOfSize(1)));

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
                        .withBody(asString("jsons/userDetailForPatient98001046534.json"))));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/" + otherHealthId + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
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
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)));
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
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(isForbidden()));
    }

    private void createNonConfidentialEncounter(String healthId, String division, String district) throws Exception {
        Patient patient = createPatient(healthId, division, district);

        final Requester createdBy = new Requester("facilityId", "providerId");
        createEncounter(createEncounterBundle(ENCOUNTER_ID, healthId, Normal, Normal, asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml"), createdBy, new Date()), patient);
    }

    private void createConfidentialEncounter(String healthId) throws Exception {
        Patient patient = createPatient(healthId, DATASENSE_REGISTERED_DIVISION, DATASENSE_REGISTERED_DISTRICT);

        final Requester createdBy = new Requester("facilityId", "providerId");
        createEncounter(createEncounterBundle(ENCOUNTER_ID, healthId, VeryRestricted, Normal, asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml"), createdBy, new Date()), patient);
    }

    private Patient createPatient(String healthId, String division, String district) {
        Patient patient = new Patient();
        patient.setHealthId(healthId);
        patient.setAddress(new Address(division, district, "03", "04", "05"));
        return patient;
    }

    private BaseMatcher<EncounterResponse> debugEncounterSaveResponse() {
        return new BaseMatcher<EncounterResponse>() {
            @Override
            public void describeTo(Description description) {
            }

            @Override
            public boolean matches(Object item) {
                if (item instanceof EncounterResponse) {
                    EncounterResponse response = (EncounterResponse) item;
                    for (org.freeshr.application.fhir.Error error : response.getErrors()) {
                        System.out.println(error.getReason());
                    }
                } else if (item instanceof UnProcessableEntity) {
                    UnProcessableEntity response = (UnProcessableEntity) item;
                    for (Error error : response.getResult().getErrors()) {
                        System.out.println(error.toString());
                    }
                }
                return false;
            }
        };
    }
}