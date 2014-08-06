package org.freeshr.domain.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.InvalidEncounter;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class EncounterServiceIntegrationTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private PatientRepository patientRepository;

    private static final String HEALTH_ID = "5dd24827-fd5d-4024-9f65-5a3c88a28af5";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/v1/patients/" + HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/conceptreferenceterm/fa460ea6-04c7-45af-a6fa-5072e7caed40"))
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

    @Test(expected = InvalidEncounter.class)
    public void shouldRejectEncounterWithInvalidReferenceCode() throws Exception {
        encounterService.ensureCreated(invalidReferenceTermEncounter("5dd24827-fd5d-4024-9f65-5a3c88a28af5")).get();
    }

    @Test(expected = InvalidEncounter.class)
    public void shouldRejectEncounterWithInvalidConceptCode() throws Exception {
        encounterService.ensureCreated(invalidConceptEncounter("5dd24827-fd5d-4024-9f65-5a3c88a28af5")).get();
    }

    @Test
    public void shouldCaptureAnEncounterAlongWithPatientDetails() throws Exception {
        givenThat(get(urlEqualTo("/api/v1/patients/" + HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient.json"))));

        String encounterId = encounterService.ensureCreated(validEncounter(HEALTH_ID)).get();

        assertThat(encounterId, is(notNullValue()));
        assertValidPatient(patientRepository.find(HEALTH_ID).get());
    }


    private void assertValidPatient(Patient patient) {
        assertThat(patient, is(notNullValue()));
        Address address = patient.getAddress();
        assertThat(address.getLine(), is("house30"));
        assertThat(address.getDistrict(), is("1004"));
        assertThat(address.getUnion(), is("10041923"));
        assertThat(address.getUpazilla(), is("100419"));
        assertThat(address.getDivision(), is("10"));
    }

    private EncounterBundle invalidReferenceTermEncounter(String healthId) {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setHealthId(healthId);
        encounterBundle.setContent(asString("jsons/invalid_ref_encounter.json"));
        return encounterBundle;
    }
    private EncounterBundle invalidConceptEncounter(String healthId) {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setHealthId(healthId);
        encounterBundle.setContent(asString("jsons/invalid_concept_encounter.json"));
        return encounterBundle;
    }

    private EncounterBundle validEncounter(String healthId) {
        EncounterBundle encounter = new EncounterBundle();
        encounter.setContent(asString("jsons/encounter.json"));
        encounter.setHealthId(healthId);
        return encounter;
    }
}