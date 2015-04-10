package org.freeshr.domain.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
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
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.freeshr.data.EncounterBundleData.withNewEncounterForPatient;
import static org.freeshr.data.EncounterBundleData.withValidEncounter;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.utils.StringUtils.ensureSuffix;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
@TestPropertySource(properties = "MCI_SERVER_URL=http://localhost:9997")
public class CatchmentEncounterServiceIntegrationTest {

    private static final String VALID_FACILITY_ID = "10000001";
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

    private static final String VALID_HEALTH_ID = "5893922485019082753";
    private static final String VALID_HEALTH_ID_NEW = "5893922485019081234";

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
                        .withBody(asString("jsons/patient_not_confidential.json"))));

        givenThat(get(urlEqualTo(ensureSuffix(shrProperties.getFRLocationPath(), "/") + VALID_FACILITY_ID + ".json"))
                .withHeader("X-Auth-Token", matching(shrProperties.getIdPAuthToken()))
                .withHeader("client_id", matching(shrProperties.getIdPClientId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/Facility.json"))));

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
    public void shouldReturnUniqueListOfEncountersForGivenListOfCatchments() throws ExecutionException,
            InterruptedException, ParseException {
        String clientId = "123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        Facility facility = new Facility("4", "facility1", "Main hospital", "305610", new Address("1", "2", "3",
                null, null));
        Date date = new Date();
        facilityRepository.save(facility).toBlocking().first();
        patientEncounterService.ensureCreated(withValidEncounter(), getUserInfo(clientId, email, securityToken)).toBlocking().first();

        List<EncounterBundle> encounterBundles = catchmentEncounterService.findEncountersForFacilityCatchment("305610",
                date, 20).toBlocking().first();
        assertEquals(1, encounterBundles.size());
        assertEquals(VALID_HEALTH_ID, encounterBundles.iterator().next().getHealthId());
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
        assertTrue(patientEncounterService.ensureCreated(withValidEncounter(), getUserInfo(clientId, email, securityToken)).toBlocking()
                .first().isSuccessful());
        assertTrue(patientEncounterService.ensureCreated(withNewEncounterForPatient(VALID_HEALTH_ID_NEW), getUserInfo(clientId, email, securityToken)).toBlocking().first()
                .isSuccessful());

        assertEquals(1, patientEncounterService.findEncountersForPatient(VALID_HEALTH_ID, null,
                200).toBlocking().first().size());

        List<EncounterBundle> encounterBundles = catchmentEncounterService.findEncountersForFacilityCatchment(
                "3056", date, 10).toBlocking().first();
        assertEquals(2, encounterBundles.size());

        ArrayList<String> healthIds = extractListOfHealthIds(encounterBundles);
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

    private UserInfo getUserInfo(String clientId, String email, String securityToken) {
        return new UserInfo(clientId, "foo", email, 1, true,
                securityToken, new ArrayList<String>(), asList(new UserProfile("facility", "10000069", asList("3026"))));
    }

    @After
    public void teardown() {
        cqlOperations.execute("truncate encounter;");
        cqlOperations.execute("truncate patient;");
        cqlOperations.execute("truncate FACILITIES;");
    }
}