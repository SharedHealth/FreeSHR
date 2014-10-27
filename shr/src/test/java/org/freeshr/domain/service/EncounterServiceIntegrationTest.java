package org.freeshr.domain.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.freeshr.util.ValidationFailures;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    private static final String VALID_FACILITY_ID = "10000001";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SHRProperties shrProperties;

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

        givenThat(get(urlEqualTo(shrProperties.getFacilityRegistryUrl() + "/" + VALID_FACILITY_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Auth-Token", shrProperties.getFacilityRegistryAuthToken())
                        .withBody(asString("jsons/Facility.json"))));

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
        cqlOperations.execute("truncate encounter;");
        cqlOperations.execute("truncate patient;");
        cqlOperations.execute("truncate FACILITIES;");
    }

    @Test
    public void shouldRejectEncounterWithInvalidReferenceCode() throws Exception {
        EncounterResponse response = encounterService.ensureCreated(withInvalidReferenceTerm(VALID_HEALTH_ID));
        assertTrue(new ValidationFailures(response).matches(new String[]{"/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "code-unknown", null}));
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptCode() throws Exception {
        EncounterResponse response = encounterService.ensureCreated(withInvalidConcept(VALID_HEALTH_ID));
        assertTrue(new ValidationFailures(response).matches(new String[]{"/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "code-unknown", "Viral pneumonia 314247"}));
    }

    @Test
    public void shouldRejectEncountersForUnknownPatients() throws ExecutionException, InterruptedException {
        EncounterResponse response = encounterService.ensureCreated(withValidEncounter(INVALID_HEALTH_ID));
        assertThat(true, is(response.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition)));
    }

    @Test
    public void shouldCaptureAnEncounterAlongWithPatientDetails() throws Exception {
        EncounterResponse response = encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID));

        assertThat(response, is(notNullValue()));
        assertTrue(response.isSuccessful());
        assertValidPatient(patientRepository.find(VALID_HEALTH_ID));
        List<EncounterBundle> encounterBundles = encounterService.findAll(VALID_HEALTH_ID);
        assertThat(encounterBundles.size(), is(1));
        assertThat(encounterBundles.get(0).getHealthId(), is(VALID_HEALTH_ID));
    }


    @Test
    public void shouldReturnEmptyListOfEncountersWhenGivenFacilityNotFound() throws ExecutionException, InterruptedException, ParseException {
        final String date = "2014-09-10";
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID_NEW));
        assertTrue(encounterService.findAllEncountersByFacilityCatchments("1", date).isEmpty());

    }

    @Test
    public void shouldReturnEmptyListOfEncountersIfFacilityIsNotPresent() throws ExecutionException, InterruptedException, ParseException {
        final String date = "2014-09-10";
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID_NEW));
        assertTrue(encounterService.findAllEncountersByFacilityCatchments("9999999999", date).isEmpty());
    }

    @Test
    @Ignore
    public void shouldReturnTheListOfEncountersForGivenListOfCatchments() throws ExecutionException, InterruptedException, ParseException {
        Facility facility1 = new Facility("1", "facility1", "Main hospital", "3056", new Address("1", "2", "3", null, null));
        Facility facility2 = new Facility("2", "facility2", "Trivial hospital", "305650", new Address("11", "22", "33", null, null));
        facilityRepository.save(facility1);
        facilityRepository.save(facility2);
        final String date = "2014-09-10";


        // Two unique encounters found in same catchment for 2 different patients
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID));
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID_NEW));

        List<EncounterBundle> encounterBundles = encounterService.findAllEncountersByFacilityCatchments("1", date);
        List<String> healthIds = extractListOfHealthIds(encounterBundles);
        assertEquals(2, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID, VALID_HEALTH_ID_NEW)));

        //Only one encounter found in a given catchment
        encounterBundles = encounterService.findAllEncountersByFacilityCatchments("2", date);
        healthIds = extractListOfHealthIds(encounterBundles);
        assertEquals(1, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID_NEW)));
    }


    @Test
    public void shouldReturnUniqueListOfEncountersForSameHealthIdGivenListOfCatchments() throws ExecutionException, InterruptedException, ParseException {
        Facility facility = new Facility("3", "facility", "Main hospital", "305610,3056", new Address("1", "2", "3", null, null));
        facilityRepository.save(facility);

        assertNotNull(facilityRepository.find("3"));
        assertTrue(encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID)).isSuccessful());
        assertTrue(encounterService.ensureCreated(withNewValidEncounter(VALID_HEALTH_ID)).isSuccessful());

        assertEquals(2, encounterService.findAll(VALID_HEALTH_ID).size());
        List<EncounterBundle> encounterBundles = encounterService.findAllEncountersByFacilityCatchments("3", "2014-09-10");
        assertEquals(2, encounterBundles.size());

        ArrayList<String> healthIds = extractListOfHealthIds(encounterBundles);
        assertEquals(2, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID, VALID_HEALTH_ID)));
    }


    @Test
    public void shouldReturnUniqueListOfEncountersForGivenListOfCatchments() throws ExecutionException, InterruptedException, ParseException {
        Facility facility = new Facility("4", "facility1", "Main hospital", "305610", new Address("1", "2", "3", null, null));
        facilityRepository.save(facility);
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID));

        String date = "2014-09-10";
        List<EncounterBundle> encounterBundles = encounterService.findAllEncountersByFacilityCatchments("4", date);
        assertEquals(1, encounterBundles.size());
        assertEquals(VALID_HEALTH_ID, encounterBundles.iterator().next().getHealthId());
    }

    @Test
    @Ignore
    public void shouldReturnSetOfEncounterByCatchment() throws ExecutionException, InterruptedException, ParseException {
        Facility facility1 = new Facility("5", "facility1", "Main hospital", "305610,3056", new Address("1", "2", "3", null, null));
        facilityRepository.save(facility1);
        encounterService.ensureCreated(withValidEncounter(VALID_HEALTH_ID));
        encounterService.ensureCreated(withNewValidEncounter(VALID_HEALTH_ID));

        String date = "2014-09-10";
        List<EncounterBundle> encounterBundles = encounterService.findAllEncountersByFacilityCatchments("5", date);
        ArrayList<String> healthIds = extractListOfHealthIds(encounterBundles);
        Collections.sort(healthIds);
        assertEquals(2, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID, VALID_HEALTH_ID)));
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
        assertThat(address.getUpazila(), is("10"));
        assertThat(address.getDivision(), is("30"));
        assertThat(address.getCityCorporation(), is("99"));
    }
}