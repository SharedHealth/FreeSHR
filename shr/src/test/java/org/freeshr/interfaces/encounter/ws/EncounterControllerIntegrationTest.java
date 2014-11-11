package org.freeshr.interfaces.encounter.ws;

import com.google.common.base.Charsets;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.domain.service.EncounterService;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

public class EncounterControllerIntegrationTest extends APIIntegrationTestBase {

    private static final String VALID_HEALTH_ID = "5893922485019082753";

    private static final String INVALID_HEALTH_ID = "1234";

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
    }

    @Test
    public void shouldSaveEncounter() throws Exception {
        mockMvc.perform(post("/patients/" + VALID_HEALTH_ID + "/encounters")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter.xml")))
                .andExpect(request().asyncResult(new InstanceOf(EncounterResponse.class)));
    }

    /*Note: Verifying status code on async result is not supported in spring test. The actual response codes are being set as expected.*/

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
    public void shouldGetEncountersForPatient() throws Exception {
        Patient patient = new Patient();
        String healthId = generateHealthId();
        patient.setHealthId(healthId);
        patient.setAddress(new Address("01", "02", "03", "04", "05"));

        createEncounter(createEncounterBundle("e-0-"+healthId, healthId), patient);
        createEncounter(createEncounterBundle("e-1-"+healthId, healthId), patient);
        createEncounter(createEncounterBundle("e-2-"+healthId, healthId), patient);
        mockMvc.perform(MockMvcRequestBuilders.get(
                String.format("/patients/%s/encounters", healthId))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(IsCollectionWithSize.hasSize(3)));
    }

    @Test
    public void shouldGetEncountersForCatchment() throws Exception {
        Patient patient1 = new Patient();
        String healthId1 = generateHealthId();
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("30", "26", "18", "01", "02"));
        createEncounter(createEncounterBundle("e-0100-"+healthId1, healthId1), patient1);
        createEncounter(createEncounterBundle("e-1100-"+healthId1, healthId1), patient1);
        createEncounter(createEncounterBundle("e-2100-"+healthId1, healthId1), patient1);

        Patient patient2 = new Patient();
        String healthId2 = generateHealthId();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("30", "26", "18", "02", "02"));
        createEncounter(createEncounterBundle("e-0200-"+healthId2, healthId2), patient2);
        createEncounter(createEncounterBundle("e-1200-"+healthId2, healthId2), patient2);
        createEncounter(createEncounterBundle("e-2200-"+healthId2, healthId2), patient2);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());

        //mockFacility("10000069");
        Facility facility = new Facility("10000069", "facility1", "Main hospital", "3026, 30261801", new Address("30", "26", "18", null, null));
        createFacility(facility);

        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + "3026" + "/encounters?updatedSince="+today)
                .header("facilityId", "10000069")
                .accept(MediaType.APPLICATION_ATOM_XML))
                .andExpect(request().asyncResult(hasEncountersOfSize(6)));



        mockMvc.perform(MockMvcRequestBuilders.get("/catchments/" + "30261801" + "/encounters?updatedSince="+today)
                .header("facilityId", "10000069")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncResult(hasEncountersOfSize(3)));
    }

    private void mockFacility(String facilityId) {
        givenThat(get(urlEqualTo(shrProperties.getFacilityRegistryUrl() + "/" + facilityId + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("X-Auth-Token", shrProperties.getFacilityRegistryAuthToken())
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + facilityId + ".json"))));
    }


    private EncounterBundle createEncounterBundle(String encounterId, String healthId) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setEncounterId(encounterId);
        bundle.setHealthId(healthId);
        bundle.setEncounterContent(asString("jsons/encounters/valid.json"));
        return bundle;
    }

}