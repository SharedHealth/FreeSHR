package org.freeshr.interfaces.encounter.ws;

import com.google.common.base.Charsets;
import net.sf.ehcache.CacheManager;
import org.freeshr.application.fhir.*;
import org.freeshr.application.fhir.Error;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.events.EncounterEvent;
import org.freeshr.interfaces.encounter.ws.exceptions.PreconditionFailed;
import org.freeshr.interfaces.encounter.ws.exceptions.Redirect;
import org.freeshr.interfaces.encounter.ws.exceptions.UnProcessableEntity;
import org.freeshr.utils.Confidentiality;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.freeshr.utils.Confidentiality.Normal;
import static org.freeshr.utils.Confidentiality.Restricted;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.HttpUtil.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997", "FACILITY_REGISTRY_URL=http://localhost:9997/facilities/", "PROVIDER_REGISTRY_URL=http://localhost:9997/providers/"})
public class PatientEncounterControllerIntegrationTest extends APIIntegrationTestBase {
    private static final String VALID_HEALTH_ID_CONFIDENTIAL = "98001046534";
    private static final String VALID_HEALTH_ID_NOT_CONFIDENTIAL = "99001046345";

    private static final String INVALID_HEALTH_ID = "1234";
    private final String validClientId = "6";
    private final String validEmail = "some@thoughtworks.com";
    private final String validAccessToken = "2361e0a8-f352-4155-8415-32adfb8c2472";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID_CONFIDENTIAL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient98001046534.json"))));

        givenThat(get(urlEqualTo("/api/default/patients/" + INVALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(404)));

        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID_NOT_CONFIDENTIAL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient99001046345.json"))));

        givenThat(get(urlEqualTo("/facilities/10019841.json"))
                .withHeader("client_id", matching("18550"))
                .withHeader("X-Auth-Token", matching("c6e6fd3a26313eb250e1019519af33e743808f5bb50428ae5423b8ee278e6fa5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility10019841.json"))));

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



        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/79647ed4-a60e-4cf5-ba68-cf4d55956cba"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/hemoglobin_diagnostic.json"))));

        givenThat(get(urlEqualTo("openmrs/ws/rest/v1/tr/concepts/a8a58344-602e-44c6-9677-841deeeb4ab4"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/viral_pneumonia_diagnostic.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/encounter-type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/encounter-type-case-insensitive.json"))));

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
    public void shouldSaveEncounter() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID_CONFIDENTIAL + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));
    }

    @Test
    public void shouldUpdateEncounter() throws Exception {
        Patient patient = new Patient();
        patient.setHealthId(VALID_HEALTH_ID_CONFIDENTIAL);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        String encounterId = UUID.randomUUID().toString();
        final Requester createdBy = new Requester("10000002", null);
        createEncounter(createEncounterBundle(encounterId, VALID_HEALTH_ID_CONFIDENTIAL,
                Confidentiality.Normal, Confidentiality.Restricted,
                asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"), createdBy, new Date()), patient);

        mockMvc.perform(put("/patients/" + VALID_HEALTH_ID_CONFIDENTIAL + "/encounters/" + encounterId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(assertEncounterResponse(encounterId)));
    }

    @Test
    public void shouldRejectEncounterUpdateForInvalidEncounterId() throws Exception {
        String invalidEncounterId = "12323";
        mockMvc.perform(put("/patients/" + VALID_HEALTH_ID_CONFIDENTIAL + "/encounters/" + invalidEncounterId)
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(request().asyncResult(new InstanceOf(PreconditionFailed.class)));
    }

    @Test
    public void shouldRejectEncounterUpdateWhenHealthIdIsInvalid() throws Exception {
        mockMvc.perform(put("/patients/" + INVALID_HEALTH_ID + "/encounters/encounterId")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p1234_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(request().asyncResult(new InstanceOf(PreconditionFailed.class)));
    }

    @Test
    public void shouldRejectAnEncounterWithCorrectHttpResponseCodeWhenHealthIdIsInvalid() throws Exception {
        mockMvc.perform(post("/patients/" + INVALID_HEALTH_ID + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p1234_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(request().asyncResult(new InstanceOf(PreconditionFailed.class)));
    }

    @Test
    public void shouldRejectAnEncounterWithCorrectHttpResponseCodeWhenThereAreValidationFailures() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID_CONFIDENTIAL + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml")))
                .andExpect(request().asyncResult(new InstanceOf(UnProcessableEntity.class)));
    }

    @Test
    public void shouldRejectAnEncounterWithInvalidDiagnosticCode() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID_CONFIDENTIAL + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidConcept.xml")))
                .andExpect(request().asyncResult(new InstanceOf(UnProcessableEntity.class)));
    }

    @Test
    public void shouldSaveTheEncounterWhenValidType() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID_CONFIDENTIAL + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_validEncType.xml")))
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));
    }

    @Test
    @Ignore
    public void shouldRejectAnEncounterWithInvalidEncounterType() throws Exception {
        //TODO: Fix this. InstanceValidator (specifically HapiWorkerContext) is overriding encounter-type valueset
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID_CONFIDENTIAL + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_inValidEncType.xml")))
                .andExpect(request().asyncResult(new InstanceOf(UnProcessableEntity.class)));
    }

    @Test
    public void shouldGetEncountersForPatient() throws Exception {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        final Requester createdBy = new Requester("facilityId", "providerId");
        createEncounter(createEncounterBundle("e-0-" + healthId, healthId, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient);
        createEncounter(createEncounterBundle("e-1-" + healthId, healthId, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient);
        createEncounter(createEncounterBundle("e-2-" + healthId, healthId, Normal, Normal, asString("jsons/encounters/valid.json"), createdBy, new Date()), patient);
        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", healthId))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(3)));
    }

    @Test
    public void shouldGetOnlyEncountersWithNormalConfidentiality() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID_NOT_CONFIDENTIAL + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p99001046345_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));

        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", VALID_HEALTH_ID_NOT_CONFIDENTIAL))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)))
                .andExpect(request().asyncResult(assertConfidentiality(Normal, Normal)));
    }

    @Test
    public void shouldGetOnlyEncountersWithConfidentialityNotSpecified() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID_NOT_CONFIDENTIAL + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p99001046345_encounter_with_diagnoses_with_localRefs_without_confidentiality.xml")))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));

        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", VALID_HEALTH_ID_NOT_CONFIDENTIAL))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(1)))
                .andExpect(request().asyncResult(assertConfidentiality(Normal, Normal)));
    }

    @Test
    public void shouldNotGetRestrictedEncounters() throws Exception {
        Patient patient1 = new Patient();
        String healthId1 = generateHealthId();
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("30", "26", "18", "01", "02"));
        final Requester createdBy = new Requester("facilityId", "providerId");
        createEncounter(createEncounterBundle("e-0100-" + healthId1, healthId1, Restricted, Normal, asString("xmls/encounters/dstu2/p99001046345_encounter_with_diagnoses_with_local_refs.xml"), createdBy, new Date()), patient1);

        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", VALID_HEALTH_ID_NOT_CONFIDENTIAL))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(hasEncounterEventsOfSize(0)));
    }

    @Test
    public void shouldRedirectResponseIfEncountersOfAMergedPatientAreRequested() throws Exception {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setMergedWith("Some health Id");
        createPatient(patient);

        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", healthId))
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(new InstanceOf(Redirect.class)))
                .andExpect(request().asyncResult(hasContent(String.format("%s has been moved and replaced with %s", healthId, "Some health Id"))));

    }

    @Test
    public void shouldRedirectResponseIfEncountersForAMergedPatientIsCreated() throws Exception {
        Patient patient = new Patient();
        String healthId = "1234";
        patient.setHealthId(healthId);
        patient.setMergedWith("Some health Id");
        createPatient(patient);

        Error inactivePatientError = new Error("healthId", "inactive", String.format("%s has been moved and replaced with %s", healthId, "Some health Id"));

        mockMvc.perform(post("/patients/" + healthId + "/encounters")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p1234_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(request().asyncResult(new InstanceOf(Redirect.class)))
                .andExpect(request().asyncResult(hasContent(asList(inactivePatientError).toString())));
    }

    @Test
    public void shouldRedirectResponseIfEncountersForAMergedPatientIsUpdated() throws Exception {
        Patient patient = new Patient();
        String healthId = "1234";
        patient.setHealthId(healthId);
        patient.setMergedWith("Some health Id");
        createPatient(patient);

        Error inactivePatientError = new Error("healthId", "inactive", String.format("%s has been moved and replaced with %s", healthId, "Some health Id"));

        mockMvc.perform(put("/patients/" + healthId + "/encounters/encounter-id")
                .header(AUTH_TOKEN_KEY, validAccessToken)
                .header(FROM_KEY, validEmail)
                .header(CLIENT_ID_KEY, validClientId)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/dstu2/p1234_encounter_with_diagnoses_with_local_refs.xml")))
                .andExpect(request().asyncResult(new InstanceOf(Redirect.class)))
                .andExpect(request().asyncResult(hasContent(asList(inactivePatientError).toString())));
    }

    private Matcher<String> hasContent(final String content) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                return ((Redirect)item).getMessage().equals(content);
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    private BaseMatcher<EncounterSearchResponse> assertConfidentiality(final Confidentiality encounterConfidentiality, final
    Confidentiality patientConfidentiality) {
        return new BaseMatcher<EncounterSearchResponse>() {
            @Override
            public void describeTo(Description description) {
            }

            @Override
            public boolean matches(Object item) {
                EncounterEvent encounterEvent = ((EncounterSearchResponse) item).getEntries().get(0);
                return encounterEvent.getEncounterBundle().getEncounterConfidentiality().equals(encounterConfidentiality) &&
                        encounterEvent.getEncounterBundle().getPatientConfidentiality().equals(patientConfidentiality);
            }
        };
    }

    private BaseMatcher<EncounterResponse> assertEncounterResponse(final String encounterId) {
        return new BaseMatcher<EncounterResponse>() {
            @Override
            public void describeTo(Description description) {
            }

            @Override
            public boolean matches(Object item) {
                return ((EncounterResponse) item).getEncounterId().equals(encounterId);
            }
        };
    }
}