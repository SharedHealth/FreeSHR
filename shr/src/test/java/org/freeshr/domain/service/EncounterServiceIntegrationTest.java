package org.freeshr.domain.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.freeshr.util.ValidationFailures;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.data.EncounterBundleData.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class EncounterServiceIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    CqlOperations cqlOperations;

    private static final String VALID_HEALTH_ID = "5dd24827-fd5d-4024-9f65-5a3c88a28af5";
    private static final String VALID_HEALTH_ID_NEW = "44e24827-fd5d-4024-9f65-5a3c88a28af5";

    private static final String INVALID_HEALTH_ID = "invalid-fd5d-4024-9f65-5a3c88a28af5";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/v1/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient.json"))));
        givenThat(get(urlEqualTo("/api/v1/patients/" + VALID_HEALTH_ID_NEW))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patientNew.json"))));

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

    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter");
    }

    @Test
    public void shouldRejectEncounterWithInvalidReferenceCode() throws Exception {
        EncounterResponse response = encounterService.ensureCreated(withInvalidReferenceTerm(VALID_HEALTH_ID)).get();
        assertTrue(new ValidationFailures(response).matches(new String[]{"/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "code-unknown", null}));
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptCode() throws Exception {
        EncounterResponse response = encounterService.ensureCreated(withInvalidConcept(VALID_HEALTH_ID)).get();
        assertTrue(new ValidationFailures(response).matches(new String[]{"/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "code-unknown", "Viral pneumonia 314247"}));
    }

    @Test
    public void shouldRejectEncountersForUnknownPatients() throws ExecutionException, InterruptedException {
        EncounterResponse response = encounterService.ensureCreated(withValidEncounter(INVALID_HEALTH_ID)).get();
        assertThat(true, is(response.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition)));
    }

    @Test
    public void shouldCaptureAnEncounterAlongWithPatientDetails() throws Exception {
        EncounterResponse response = encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID)).get();

        assertThat(response, is(notNullValue()));
        assertTrue(response.isSuccessful());
        assertValidPatient(patientRepository.find(VALID_HEALTH_ID).get());

    }

    @Test
    public void shouldReturnTheListOfEncountersForGivenListOfCatchments() throws ExecutionException, InterruptedException {
        Facility facility1 = new Facility("1", "far", "3056");
        Facility facility2 = new Facility("2", "foo", "305650");
        facilityRepository.save(facility1);
        facilityRepository.save(facility2);

        // Two unique encounters found in same catchment for 2 different patients
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID)).get();
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID_NEW)).get();

        Thread.sleep(2000);
//        List<String> catchments= Arrays.asList("3056");
        List<EncounterBundle> encounterBundles = encounterService.findAllEncountersByCatchments("1").get();
        List<String> healthIds = extractListOfHealthIds(encounterBundles);
        assertEquals(2, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID, VALID_HEALTH_ID_NEW)));

        //Only one encounter found in a given catchment
        //catchments= Arrays.asList("305650");
        encounterBundles = encounterService.findAllEncountersByCatchments("2").get();
        healthIds = extractListOfHealthIds(encounterBundles);
        assertEquals(1, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID_NEW)));

    }


    @Test
    public void shouldReturnUniqueListOfEncountersForSameHealthIdGivenListOfCatchments() throws ExecutionException, InterruptedException {
        Facility facility1 = new Facility("1", "far", "3056");
        facilityRepository.save(facility1);
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID)).get();
        encounterService.ensureCreated(withNewValidEncounter(VALID_HEALTH_ID)).get();

        List<EncounterBundle> encounterBundles = encounterService.findAllEncountersByCatchments("1").get();
        ArrayList<String> healthIds = extractListOfHealthIds(encounterBundles);
        Collections.sort(healthIds);
        assertEquals(2, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID, VALID_HEALTH_ID)));
    }

    @Test
    public void shouldReturnUniqueListOfEncountersForGivenListOfCatchments() throws ExecutionException, InterruptedException {
        Facility facility2 = new Facility("2", "foo", "305610");
        facilityRepository.save(facility2);
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID)).get();

        List<EncounterBundle> encounterBundles = encounterService.findAllEncountersByCatchments("2").get();
        assertEquals(1, encounterBundles.size());
        assertEquals(VALID_HEALTH_ID,encounterBundles.iterator().next().getHealthId());
    }

    private ArrayList<String> extractListOfHealthIds(List<EncounterBundle> encounterBundles) {
        ArrayList<String> healthIds = new ArrayList<>();
        for (EncounterBundle encounterBundle : encounterBundles) {
            healthIds.add(encounterBundle.getHealthId());
        }
        return healthIds;
    }

    private void assertValidPatient(Patient patient) {
        assertThat(patient, is(notNullValue()));
        Address address = patient.getAddress();
        assertThat(address.getLine(), is("house30"));
        assertThat(address.getDistrict(), is("56"));
        assertThat(address.getWard(), is("17"));
        assertThat(address.getUpazilla(), is("10"));
        assertThat(address.getDivision(), is("30"));
        assertThat(address.getCityCorporation(), is("99"));
    }
}