package org.freeshr.domain.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.ehcache.CacheManager;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.events.EncounterEvent;
import org.freeshr.infrastructure.persistence.FacilityRepository;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.freeshr.data.EncounterBundleData.withContentForHealthId;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997", "FACILITY_REGISTRY_URL=http://localhost:9997/facilities/", "PROVIDER_REGISTRY_URL=http://localhost:9997/providers/"})
public class CatchmentEncounterServiceIntegrationTest {

    private static final String VALID_FACILITY_ID = "10019841";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Autowired
    private CatchmentEncounterService catchmentEncounterService;

    @Autowired
    private PatientEncounterService patientEncounterService;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SHRProperties shrProperties;

    @Autowired
    @Qualifier("SHRCassandraTemplate")
    CqlOperations cqlOperations;

    private static final String VALID_HEALTH_ID = "98001046534";
    private static final String VALID_HEALTH_ID_NEW = "99001046345";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient.json"))));

        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID_NEW))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient_not_confidential.json"))));

        givenThat(get(urlEqualTo("/facilities/" + VALID_FACILITY_ID + ".json"))
                .withHeader("client_id", matching(shrProperties.getIdPClientId()))
                .withHeader("X-Auth-Token", matching(shrProperties.getIdPAuthToken()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility10000069.json"))));

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
    }

    @Test
    public void shouldReturnUniqueListOfEncountersForGivenListOfCatchments() throws ExecutionException,
            InterruptedException, ParseException {
        String clientId = "123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        Facility facility = new Facility("4", "facility1", "Main hospital", "305610", new Address("1", "2", "3",
                null, null));
        Date date = new Date();
        facilityRepository.save(facility).toBlocking().first();
        patientEncounterService.ensureCreated(
                withContentForHealthId(VALID_HEALTH_ID, "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"),
                getUserInfo(clientId, email, securityToken)).toBlocking().first();

        List<EncounterEvent> encounterEvents = catchmentEncounterService.findEncounterFeedForFacilityCatchment("305610", date, null).toBlocking().first();
        assertEquals(1, encounterEvents.size());
        assertEquals(VALID_HEALTH_ID, encounterEvents.iterator().next().getHealthId());
    }

    @Test
    public void shouldReturnUniqueListOfEncountersForFacilityCatchment() throws ExecutionException,
            InterruptedException, ParseException {
        String clientId = "123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        Date date = new Date();

        Facility facility = new Facility("3", "facility", "Main hospital", "305610,3056", new Address("1", "2", "3",
                null, null));
        facilityRepository.save(facility).toBlocking().first();

        assertNotNull(facilityRepository.find("3").toBlocking().first());
        EncounterResponse response = patientEncounterService.ensureCreated(
                withContentForHealthId(VALID_HEALTH_ID, "xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"),
                getUserInfo(clientId, email, securityToken)).toBlocking()
                .first();
        assertTrue(response.isSuccessful());
        assertTrue(patientEncounterService.ensureCreated(
                withContentForHealthId(VALID_HEALTH_ID_NEW, "xmls/encounters/dstu2/p99001046345_encounter_with_diagnoses_with_local_refs.xml"),
                getUserInfo(clientId, email, securityToken)).toBlocking().first()
                .isSuccessful());

        assertEquals(1, patientEncounterService.getEncounterFeedForPatient(VALID_HEALTH_ID, null,
                200).toBlocking().first().size());

        List<EncounterEvent> encounterEvents = catchmentEncounterService.findEncounterFeedForFacilityCatchment(
                "3056", date, null).toBlocking().first();
        assertEquals(2, encounterEvents.size());

        List<String> healthIds = extract(encounterEvents, on(EncounterEvent.class).getHealthId());
        assertEquals(2, healthIds.size());
        assertTrue(healthIds.containsAll(Arrays.asList(VALID_HEALTH_ID, VALID_HEALTH_ID)));
    }

    private UserInfo getUserInfo(String clientId, String email, String securityToken) {
        return new UserInfo(clientId, "foo", email, 1, true,
                securityToken, new ArrayList<String>(), asList(new UserProfile("facility", "10000069", asList("3026"))));
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