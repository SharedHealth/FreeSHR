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
import rx.Observable;
import rx.observers.TestSubscriber;

import java.text.ParseException;
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

    private static final String VALID_HEALTH_ID = "5893922485019082753";
    private static final String VALID_HEALTH_ID_NEW = "5893922485019081234";

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
                .withHeader("X-Auth-Token", matching(shrProperties.getIdPAuthToken()))
                .withHeader("client_id", matching(shrProperties.getIdPClientId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
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
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        EncounterResponse response = encounterService.ensureCreated(withInvalidReferenceTerm(), clientId, email, securityToken)
                .toBlocking().first();
        assertTrue(new ValidationFailures(response).matches(new
                String[]{"/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "code-unknown", null}));
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptCode() throws Exception {
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        EncounterResponse response = encounterService.ensureCreated(withInvalidConcept(), clientId, email, securityToken).toBlocking()
                .first();
        assertTrue(new ValidationFailures(response).matches(new
                String[]{"/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "code-unknown",
                "Viral pneumonia 314247"}));
    }

    @Test
    public void shouldRejectEncountersForUnknownPatients() throws ExecutionException, InterruptedException {
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        Observable<EncounterResponse> encounterResponseObservable = encounterService.ensureCreated
                (encounterForUnknownPatient(), clientId, email, securityToken);
        EncounterResponse response = encounterResponseObservable.toBlocking().first();
        assertThat(true, is(response.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition)));
    }

    @Test
    public void shouldCaptureAnEncounterAlongWithPatientDetails() throws Exception {
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        Observable<EncounterResponse> response = encounterService.ensureCreated(withValidEncounter(), clientId, email, securityToken);
        TestSubscriber<EncounterResponse> encounterResponseSubscriber = new TestSubscriber<>();
        response.subscribe(encounterResponseSubscriber);
        encounterResponseSubscriber.awaitTerminalEvent();
        encounterResponseSubscriber.assertNoErrors();
        EncounterResponse encounterResponse = encounterResponseSubscriber.getOnNextEvents().get(0);
        assertNotNull(encounterResponse);

        Observable<Patient> patientObservable = patientRepository.find(VALID_HEALTH_ID);
        TestSubscriber<Patient> patientTestSubscriber = new TestSubscriber<>();
        patientObservable.subscribe(patientTestSubscriber);
        patientTestSubscriber.awaitTerminalEvent();
        assertValidPatient(patientTestSubscriber.getOnNextEvents().get(0));

        Observable<List<EncounterBundle>> encountersForPatientObservable = encounterService.findEncountersForPatient
                (VALID_HEALTH_ID, null, 200);
        TestSubscriber<List<EncounterBundle>> encounterBundleTestSubscriber = new TestSubscriber<>();
        encountersForPatientObservable.subscribe(encounterBundleTestSubscriber);
        encounterBundleTestSubscriber.awaitTerminalEvent();

        List<EncounterBundle> encounterBundles = encounterBundleTestSubscriber.getOnNextEvents().get(0);
        assertThat(encounterBundles.size(), is(1));
        assertThat(encounterBundles.get(0).getHealthId(), is(VALID_HEALTH_ID));
    }

    @Test
    public void shouldReturnUniqueListOfEncountersForGivenListOfCatchments() throws ExecutionException,
            InterruptedException, ParseException {
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        Facility facility = new Facility("4", "facility1", "Main hospital", "305610", new Address("1", "2", "3",
                null, null));
        Date date = new Date();
        facilityRepository.save(facility).toBlocking().first();
        encounterService.ensureCreated(withValidEncounter(), clientId, email, securityToken).toBlocking().first();

        List<EncounterBundle> encounterBundles = encounterService.findEncountersForFacilityCatchment("4", "305610",
                date, 20).toBlocking().first();
        assertEquals(1, encounterBundles.size());
        assertEquals(VALID_HEALTH_ID, encounterBundles.iterator().next().getHealthId());
    }

    @Test
    public void shouldReturnUniqueListOfEncountersForFacilityCatchment() throws ExecutionException,
            InterruptedException, ParseException {
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        Date date = new Date();

        Facility facility = new Facility("3", "facility", "Main hospital", "305610,3056", new Address("1", "2", "3",
                null, null));
        facilityRepository.save(facility).toBlocking().first();

        assertNotNull(facilityRepository.find("3").toBlocking().first());
        assertTrue(encounterService.ensureCreated(withValidEncounter(), clientId, email, securityToken).toBlocking().first().isSuccessful());
        assertTrue(encounterService.ensureCreated(withNewValidEncounter(VALID_HEALTH_ID_NEW), clientId, email, securityToken).toBlocking().first()
                .isSuccessful());

        assertEquals(1, encounterService.findEncountersForPatient(VALID_HEALTH_ID, null,
                200).toBlocking().first().size());

        List<EncounterBundle> encounterBundles = encounterService.findEncountersForFacilityCatchment(
                "3", "3056", date, 10).toBlocking().first();
        assertEquals(2, encounterBundles.size());

        ArrayList<String> healthIds = extractListOfHealthIds(encounterBundles);
        assertEquals(2, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID, VALID_HEALTH_ID)));
    }

    @Test
    public void shouldReturnEncounterIfPresentForHealthId() throws ExecutionException, InterruptedException {
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        Facility facility = new Facility("3", "facility", "Main hospital", "305610,3056", new Address("1", "2", "3",
                null, null));
        facilityRepository.save(facility).toBlocking().first();

        EncounterResponse first = encounterService.ensureCreated(withNewValidEncounter(VALID_HEALTH_ID_NEW), clientId, email, securityToken)
                .toBlocking().first();
        String encounterId = first.getEncounterId();
        EncounterBundle encounterBundle = encounterService.findEncounter(VALID_HEALTH_ID_NEW,
                encounterId).toBlocking().first();
        assertEquals(encounterId, encounterBundle.getEncounterId());
    }

    @Test
    public void shouldReturnEmptyIfNotPresentForHealthId() throws ExecutionException, InterruptedException {
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        Facility facility = new Facility("3", "facility", "Main hospital", "305610,3056", new Address("1", "2", "3",
                null, null));
        
        facilityRepository.save(facility).toBlocking().first();

        EncounterResponse first = encounterService.ensureCreated(withNewValidEncounter(VALID_HEALTH_ID_NEW), clientId, email, securityToken)
                .toBlocking().first();
        String encounterId = first.getEncounterId();

        EncounterBundle encounterBundle = encounterService.findEncounter(INVALID_HEALTH_ID,
                encounterId).toBlocking().firstOrDefault(null);
        assertNull("Should have returned empty encounter for invalid healthId", encounterBundle);

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
        assertThat(address.getUnionOrUrbanWardId(), is("17"));
        assertThat(address.getUpazila(), is("10"));
        assertThat(address.getDivision(), is("30"));
        assertThat(address.getCityCorporation(), is("99"));
    }


}