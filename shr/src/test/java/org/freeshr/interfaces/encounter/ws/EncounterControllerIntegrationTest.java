package org.freeshr.interfaces.encounter.ws;

import com.google.common.base.Charsets;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.security.TokenAuthentication;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import org.freeshr.utils.Confidentiality;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.freeshr.utils.FileUtil.asString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@TestPropertySource(properties = "MCI_SERVER_URL=http://localhost:9997")
public class EncounterControllerIntegrationTest extends APIIntegrationTestBase {

    private static final String VALID_HEALTH_ID = "5893922485019082753";

    private static final String INVALID_HEALTH_ID = "1234";

    @Autowired
    SHRProperties properties;

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/v1/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient.json"))));

        givenThat(get(urlEqualTo("/api/v1/patients/" + INVALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(404)));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/referenceterms/fa460ea6-04c7-45af-a6fa-5072e7caed40"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/refterm.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/eddb01eb-61fc-4f9e-aca5-e44193509f35"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept.json"))));

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

        //propertiesAccessor = new SHRPropertiesAccessor(properties);
        //propertiesAccessor.updateMCIServerUrls("http://localhost:9997");

    }

    @Test
    public void shouldSaveEncounter() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_to_save.xml")))
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));
    }

    /*Note: Verifying status code on async result is not supported in spring test. The actual response codes are
    being set as expected.*/

    @Test
    public void shouldRejectAnEncounterWithCorrectHttpResponseCodeWhenHealthIdIsInvalid() throws Exception {
        mockMvc.perform(post("/patients/" + INVALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_health_id_1234.xml")))
                .andExpect(request().asyncResult(new InstanceOf(PreconditionFailed.class)));
    }

    @Test
    public void shouldRejectAnEncounterWithCorrectHttpResponseCodeWhenThereAreValidationFailures() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/diagnosis_category_invalid.xml")))
                .andExpect(request().asyncResult(new InstanceOf(UnProcessableEntity.class)));
    }

    @Test
    public void shouldRejectAnEncounterWithInvalidDiagnosticCode() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_with_invalid_diagnostic_code.xml")))
                .andExpect(request().asyncResult(new InstanceOf(UnProcessableEntity.class)));
    }

    @Test
    public void shouldSaveTheEncounterWhenValidType() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_with_valid_type_and_local_patient.xml")))
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));
    }

    @Test
    public void shouldRejectAnEncounterWithInvalidEncounterType() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_with_invalid_type.xml")))
                .andExpect(request().asyncResult(new InstanceOf(UnProcessableEntity.class)));
    }

    @Test
    public void shouldGetEncountersForPatient() throws Exception {
        UserProfile patientProfile = new UserProfile("patient", "12", null);
        UserInfo patientUser = new UserInfo("123", "name", "google@rajanikant.com", 1, true, "xyz", asList("MCI_ADMIN"), asList(patientProfile));
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(patientUser, true));

        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        createEncounter(createEncounterBundle("e-0-" + healthId, healthId), patient);
        createEncounter(createEncounterBundle("e-1-" + healthId, healthId), patient);
        createEncounter(createEncounterBundle("e-2-" + healthId, healthId), patient);
        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", healthId))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(hasEncountersOfSize(3)));
    }

    @Test
    public void shouldGetEncountersForCatchment() throws Exception {
        UserProfile patientProfile = new UserProfile("patient", "12", null);
        UserInfo patientUser = new UserInfo("123", "name", "google@rajanikant.com", 1, true, "xyz", asList("MCI_ADMIN"), asList(patientProfile));
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(patientUser, true));

        Patient patient1 = new Patient();
        String healthId1 = generateHealthId();
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("30", "26", "18", "01", "02"));
        createEncounter(createEncounterBundle("e-0100-" + healthId1, healthId1), patient1);
        createEncounter(createEncounterBundle("e-1100-" + healthId1, healthId1), patient1);
        createEncounter(createEncounterBundle("e-2100-" + healthId1, healthId1), patient1);

        Patient patient2 = new Patient();
        String healthId2 = generateHealthId();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("30", "26", "18", "02", "02"));
        createEncounter(createEncounterBundle("e-0200-" + healthId2, healthId2), patient2);
        createEncounter(createEncounterBundle("e-1200-" + healthId2, healthId2), patient2);
        createEncounter(createEncounterBundle("e-2200-" + healthId2, healthId2), patient2);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());

        Facility facility = new Facility("10000069", "facility1", "Main hospital", "3026, 30261801",
                new Address("30", "26", "18", null, null));
        createFacility(facility);

        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + "3026" + "/encounters?updatedSince=" + today)
                .header("facilityId", "10000069")
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncountersOfSize(6)));


        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + "30261801" + "/encounters?updatedSince=" + today)
                .header("facilityId", "10000069")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(hasEncountersOfSize(3)));
    }

    @Test
    public void shouldSaveEncounterConfidentialityAsNormal() throws Exception {
        UserProfile patientProfile = new UserProfile("patient", "12", null);
        UserInfo patientUser = new UserInfo("123", "name", "google@rajanikant.com", 1, true, "xyz", asList("MCI_ADMIN"), asList(patientProfile));
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(patientUser, true));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_with_normal_confidentiality.xml")))
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));

        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", VALID_HEALTH_ID))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(assertConfidentiality(Confidentiality.Normal, Confidentiality.VeryRestricted)));
    }

    @Test
    public void shouldSaveEncounterConfidentialityAsVeryRestricted() throws Exception {
        UserProfile patientProfile = new UserProfile("patient", "12", null);
        UserInfo patientUser = new UserInfo("123", "name", "google@rajanikant.com", 1, true, "xyz", asList("MCI_ADMIN"), asList(patientProfile));
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(patientUser, true));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter_with_very_restricted_confidentiality.xml")))
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));

        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", VALID_HEALTH_ID))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(assertConfidentiality(Confidentiality.VeryRestricted, Confidentiality.VeryRestricted)));
    }

    @Test
    public void shouldSaveEncounterConfidentialityAsNormalWhenNotSpecified() throws Exception {
        UserProfile patientProfile = new UserProfile("patient", "12", null);
        UserInfo patientUser = new UserInfo("123", "name", "google@rajanikant.com", 1, true, "xyz", asList("MCI_ADMIN"), asList(patientProfile));
        SecurityContextHolder.getContext().setAuthentication(new TokenAuthentication(patientUser, true));

        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter.xml")))
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));

        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", VALID_HEALTH_ID))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(assertConfidentiality(Confidentiality.Normal, Confidentiality.VeryRestricted)));
    }

    private BaseMatcher<EncounterSearchResponse> assertConfidentiality(final Confidentiality encounterConfidentiality, final Confidentiality patientConfidentiality) {
        return new BaseMatcher<EncounterSearchResponse>() {
            @Override
            public void describeTo(Description description) {
            }

            @Override
            public boolean matches(Object item) {
                EncounterBundle encounterBundle = ((EncounterSearchResponse) item).getEntries().get(0);
                return encounterBundle.getEncounterConfidentiality().equals(encounterConfidentiality) &&
                        encounterBundle.getPatientConfidentiality().equals(patientConfidentiality);
            }
        };
    }

    private void mockFacility(String facilityId) {
        givenThat(get(urlEqualTo(shrProperties.getFRLocationPath() + "/" + facilityId + ".json"))
                .withHeader("X-Auth-Token", matching(shrProperties.getIdPAuthToken()))
                .withHeader("client_id", matching(shrProperties.getIdPClientId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + facilityId + ".json"))));
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

}