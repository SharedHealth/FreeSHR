package org.freeshr.domain.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.ehcache.CacheManager;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.events.EncounterEvent;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import org.freeshr.interfaces.encounter.ws.APIIntegrationTestBase;
import org.freeshr.util.EncounterResponseFailures;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.freeshr.data.EncounterBundleData.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997", "FACILITY_REGISTRY_URL=http://localhost:9997/facilities/", "PROVIDER_REGISTRY_URL=http://localhost:9997/providers/"})
public class PatientEncounterServiceIntegrationTest extends APIIntegrationTestBase{

    private static final String VALID_FACILITY_ID = "10019841";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private PatientEncounterService patientEncounterService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private SHRProperties shrProperties;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    CqlOperations cqlOperations;

    private static final String VALID_HEALTH_ID = "98001046534";
    private static final String VALID_HEALTH_ID_NEW = "5893922485019081234";

    private static final String INVALID_HEALTH_ID = "invalid-fd5d-4024-9f65-5a3c88a28af5";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient98001046534.json"))));

        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID_NEW))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient_not_confidential.json"))));

        givenThat(get(urlEqualTo("/api/default/patients/" + INVALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(404)));

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

        Facility facility = new Facility(VALID_FACILITY_ID, "facility1", "Main hospital", "3026, 30261801",
                new Address("30", "26", "18", null, null));
        createFacility(facility);

    }

    @Test
    public void shouldRejectEncounterWithInvalidReferenceCode() throws Exception {
        String clientId = shrProperties.getIdPClientId();
        String email = "email@gmail.com";
        String securityToken = shrProperties.getIdPAuthToken();

        EncounterResponse response = patientEncounterService.ensureCreated(
                withContentForHealthId(EncounterBundleData.HEALTH_ID, "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidRefTerm.xml"),
                getUserInfo(clientId, email, securityToken))
                .toBlocking().first();
     

        assertTrue(new EncounterResponseFailures(response).matches(new
                String[]{"/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding[1]", "error",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/referenceterms/2f6z9872-4df1-438e-9d72-0a8b161d409b], code[INVALID-A90]"}));
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptCode() throws Exception {
        String clientId = shrProperties.getIdPClientId();
        String email = "email@gmail.com";
        String securityToken = shrProperties.getIdPAuthToken();

        EncounterResponse response = patientEncounterService.ensureCreated(
                withContentForHealthId(EncounterBundleData.HEALTH_ID, "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidConcept.xml"),
                getUserInfo(clientId, email, securityToken))
                .toBlocking().first();
        assertTrue(new EncounterResponseFailures(response).matches(new
                String[]{"/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding", "error",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca], code[INVALID-07952dc2-5206-11e5-ae6d-0050568225ca]"}));
    }

    @Test
    public void shouldRejectEncounterUpdateWithInvalidConceptCode() throws Exception {
        String clientId = shrProperties.getIdPClientId();
        String email = "email@gmail.com";
        String securityToken = shrProperties.getIdPAuthToken();
        UserInfo userInfo = getUserInfo(clientId, email, securityToken);
        EncounterResponse encounterCreateResponse = patientEncounterService.ensureCreated(
                withContentForHealthId(VALID_HEALTH_ID, "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"),
                userInfo)
                .toBlocking().first();

        EncounterResponse encounterUpdateResponse = patientEncounterService.ensureUpdated(
                withContentForHealthId(EncounterBundleData.HEALTH_ID, "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidConcept.xml"),
                userInfo)
                .toBlocking().first();
        assertTrue(new EncounterResponseFailures(encounterUpdateResponse).matches(new
                String[]{"/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding", "error",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca], code[INVALID-07952dc2-5206-11e5-ae6d-0050568225ca]"}));
    }

    @Test
    public void shouldRejectEncountersForUnknownPatients() throws ExecutionException, InterruptedException {
        String clientId = shrProperties.getIdPClientId();
        String email = "email@gmail.com";
        String securityToken = shrProperties.getIdPAuthToken();

        EncounterResponse encounterCreateResponse = patientEncounterService.ensureCreated(
                withContentForHealthId("99001046345", "xmls/encounters/dstu2/p99001046345_encounter_with_diagnoses_with_local_refs.xml"),
                getUserInfo(clientId, email, securityToken))
                .toBlocking().first();

//        Observable<EncounterResponse> encounterResponseObservable = patientEncounterService.ensureCreated
//                (encounterForUnknownPatient(),getUserInfo(clientId, email, securityToken));
//        EncounterResponse response = encounterResponseObservable.toBlocking().first();
        assertThat(true, is(encounterCreateResponse.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition)));
    }

    @Test
    public void shouldCaptureAnEncounterAlongWithPatientDetails() throws Exception {
        String clientId = shrProperties.getIdPClientId();
        String email = "email@gmail.com";
        String securityToken = shrProperties.getIdPAuthToken();

        Observable<EncounterResponse> response = patientEncounterService.ensureCreated(
                withContentForHealthId("98001046534", "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"),
                getUserInfo(clientId, email, securityToken));
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

        Observable<List<EncounterEvent>> encounterEventsForPatientObservable = patientEncounterService.getEncounterFeedForPatient(VALID_HEALTH_ID, null, 200);
        TestSubscriber<List<EncounterEvent>> encounterBundleTestSubscriber = new TestSubscriber<>();
        encounterEventsForPatientObservable.subscribe(encounterBundleTestSubscriber);
        encounterBundleTestSubscriber.awaitTerminalEvent();

        List<EncounterEvent> encounterEvents = encounterBundleTestSubscriber.getOnNextEvents().get(0);
        assertThat(encounterEvents.size(), is(1));
        assertThat(encounterEvents.get(0).getHealthId(), is(VALID_HEALTH_ID));
    }

    @Test
    public void shouldReturnEncounterIfPresentForHealthId() throws ExecutionException, InterruptedException {
        String clientId = shrProperties.getIdPClientId();
        String email = "email@gmail.com";
        String securityToken = shrProperties.getIdPAuthToken();

        EncounterResponse first = patientEncounterService.ensureCreated(
                withContentForHealthId("98001046534", "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"),
                getUserInfo(clientId, email, securityToken))
                .toBlocking().first();
        String encounterId = first.getEncounterId();
        EncounterBundle encounterBundle = patientEncounterService.findEncounter(VALID_HEALTH_ID,
                encounterId).toBlocking().first();
        assertEquals(encounterId, encounterBundle.getEncounterId());
    }

    @Test
    public void shouldReturnEmptyIfNotPresentForHealthId() throws ExecutionException, InterruptedException {
        String clientId = shrProperties.getIdPClientId();
        String email = "email@gmail.com";
        String securityToken = shrProperties.getIdPAuthToken();

        EncounterResponse first = patientEncounterService.ensureCreated(
                withContentForHealthId("98001046534", "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"),
                getUserInfo(clientId, email, securityToken))
                .toBlocking().first();
        String encounterId = first.getEncounterId();

        EncounterBundle encounterBundle = patientEncounterService.findEncounter(INVALID_HEALTH_ID,
                encounterId).toBlocking().firstOrDefault(null);
        assertNull("Should have returned empty encounter for invalid healthId", encounterBundle);

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

    private UserInfo getUserInfo(String clientId, String email, String securityToken) {
        return new UserInfo(clientId, "foo", email, 1, true,
                securityToken, new ArrayList<String>(), asList(new UserProfile("facility", "10000069", asList("302618"))));
    }

    @After
    public void teardown() {
        CacheManager.getInstance().clearAll();
        cqlOperations.execute("truncate encounter;");
        cqlOperations.execute("truncate patient;");
        cqlOperations.execute("truncate enc_by_catchment;");
        cqlOperations.execute("truncate enc_by_patient;");
        cqlOperations.execute("truncate FACILITIES;");
    }
}