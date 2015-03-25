package org.freeshr.utils;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRProperties;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.AccessFilter.*;
import static org.freeshr.infrastructure.security.UserInfo.*;
import static org.freeshr.infrastructure.security.UserProfile.*;
import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AccessFilterTest {
    private final String DATASENSE_FACILITY_ID = "10022222";
    private final String HEALTH_ID = "12345567890";
    private final String HEALTH_ID_FOR_PROVIDER = "22345567892";
    private final String PROVIDER_ID = "343234";
    private final String FACILITY_ID = "10000069";
    private final String FACILITY_CATCHMENT = "302618";
    private final String PROVIDER_CATCHMENT = "3026";
    private UserInfo facility;
    private UserInfo datasenseFacility;
    private UserInfo facilityAndProvider;
    private UserInfo patient;
    private UserInfo providerAndPatient;
    private UserInfo provider;

    @Mock
    private SHRProperties shrProperties;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        facility = new UserInfo("1", "bahmni", "bahmni@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(FACILITY_ADMIN_GROUP, SHR_USER_GROUP)),
                asList(new UserProfile(FACILITY_TYPE, FACILITY_ID, asList(FACILITY_CATCHMENT))))
                .loadUserProperties();
        datasenseFacility = new UserInfo("2", "datasense", "datasense@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(FACILITY_ADMIN_GROUP, SHR_USER_GROUP, DATASENSE_FACILITY_GROUP)),
                asList(new UserProfile(FACILITY_TYPE, DATASENSE_FACILITY_ID, asList(FACILITY_CATCHMENT))))
                .loadUserProperties();
        facilityAndProvider = new UserInfo("3", "facilityandprovider", "facilityandprovider@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList("Facility Admin", SHR_USER_GROUP)),
                asList(new UserProfile(FACILITY_TYPE, FACILITY_ID, asList(FACILITY_CATCHMENT)),
                        new UserProfile("provider", PROVIDER_ID, asList(PROVIDER_CATCHMENT))))
                .loadUserProperties();
        patient = new UserInfo("4", "patient", "patient@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(SHR_USER_GROUP)),
                asList(new UserProfile(PATIENT_TYPE, HEALTH_ID, null)))
                .loadUserProperties();
        providerAndPatient = new UserInfo("5", "providerAndPatient", "providerAndPatient@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(SHR_USER_GROUP)),
                asList(new UserProfile(PROVIDER_TYPE, PROVIDER_ID, asList(PROVIDER_CATCHMENT)),
                        new UserProfile(PATIENT_TYPE, HEALTH_ID_FOR_PROVIDER, null)))
                .loadUserProperties();
        provider = new UserInfo("6", "provider", "provider@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(SHR_USER_GROUP)),
                asList(new UserProfile(PROVIDER_TYPE, PROVIDER_ID, asList(PROVIDER_CATCHMENT))))
                .loadUserProperties();
    }

    @Test
    public void shouldAllowAccessForEncounterPushForFacility() throws Exception {
        assertTrue(validateAccessToSaveEncounter(facility));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForEncounterPushForDatasense() throws Exception {
        validateAccessToSaveEncounter(datasenseFacility);
    }

    @Test
    public void shouldAllowAccessForEncounterPushForFacilityAndProvider() throws Exception {
        assertTrue(validateAccessToSaveEncounter(facilityAndProvider));
    }

    @Test
    public void shouldAllowAccessForEncounterPushForProviderWithPatient() throws Exception {
        assertTrue(validateAccessToSaveEncounter(providerAndPatient));
    }

    @Test
    public void shouldAllowAccessForEncounterPushForProvider() throws Exception {
        assertTrue(validateAccessToSaveEncounter(provider));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToFacility() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForPatient("some health id", facility));
    }

    @Test
    public void shouldAllowUnRestrictedAccessForEncounterFetchToDatasense() throws Exception {
        assertFalse(isAccessRestrictedToEncounterFetchForPatient("some health id", datasenseFacility));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToFacilityWithProvider() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForPatient("some health id", facilityAndProvider));
    }

    @Test
    public void shouldAllowUnRestrictedAccessForEncounterFetchToPatientForAssociatedHid() throws Exception {
        assertFalse(isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID, patient));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForEncounterFetchToPatientForOtherHid() throws Exception {
        isAccessRestrictedToEncounterFetchForPatient("some hid", patient);
    }

    @Test
    public void shouldAllowUnRestrictedAccessForEncounterFetchToProviderWithPatientForAssociatedHid() throws Exception {
        assertFalse(isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID_FOR_PROVIDER, providerAndPatient));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToProviderWithPatientForOtherHid() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID, providerAndPatient));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToProvider() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID, provider));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToFacilityWithAssociatedCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment(FACILITY_CATCHMENT, facility));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForCatchmentFetchToFacilityWithOtherCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, facility));
    }

    @Test
    public void shouldAllowUnRestrictedAccessForCatchmentFetchToDatasenseWithAssociatedCatchment() throws Exception {
        assertFalse(isAccessRestrictedToEncounterFetchForCatchment(FACILITY_CATCHMENT, datasenseFacility));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForCatchmentFetchToDatasenseForOtherCatchment() throws Exception {
        assertFalse(isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, datasenseFacility));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToFacilityAndProviderWithFacilityCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment(FACILITY_CATCHMENT, facilityAndProvider));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToFacilityAndProviderWithProviderCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, facilityAndProvider));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForCatchmentFetchToFacilityAndProviderWithOtherCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment("1029", facilityAndProvider));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForCatchmentFetchToPatient() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment("1029", patient));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToProviderAndPatientWithAssociatedCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, providerAndPatient));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForCatchmentFetchToProviderAndPatientWithOtherCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment("1029", providerAndPatient));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToProviderWithAssociatedCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, provider));
    }

    @Test(expected = Forbidden.class)
    public void shouldNotAllowAccessForCatchmentFetchToProviderWithOtherCatchment() throws Exception {
        assertTrue(isAccessRestrictedToEncounterFetchForCatchment("1029", provider));
    }

    @Test
    public void shouldRestrictAccessToEncounterBundleWithEncounterConfidentiality() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setPatientConfidentiality(Confidentiality.Normal);

        encounterBundle.setEncounterConfidentiality(Confidentiality.Unrestricted);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setEncounterConfidentiality(Confidentiality.Low);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setEncounterConfidentiality(Confidentiality.Moderate);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setEncounterConfidentiality(Confidentiality.Normal);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setEncounterConfidentiality(Confidentiality.Restricted);
        assertTrue(isConfidentialEncounter(encounterBundle));

        encounterBundle.setEncounterConfidentiality(Confidentiality.VeryRestricted);
        assertTrue(isConfidentialEncounter(encounterBundle));
    }

    @Test
    public void shouldRestrictAccessToEncounterBundleWithPatientConfidentiality() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setEncounterConfidentiality(Confidentiality.Normal);

        encounterBundle.setPatientConfidentiality(Confidentiality.Unrestricted);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setPatientConfidentiality(Confidentiality.Low);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setPatientConfidentiality(Confidentiality.Moderate);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setPatientConfidentiality(Confidentiality.Normal);
        assertFalse(isConfidentialEncounter(encounterBundle));

        encounterBundle.setPatientConfidentiality(Confidentiality.Restricted);
        assertTrue(isConfidentialEncounter(encounterBundle));

        encounterBundle.setPatientConfidentiality(Confidentiality.VeryRestricted);
        assertTrue(isConfidentialEncounter(encounterBundle));
    }

    @Test
    public void shouldCheckPatientConfidentiality() throws Exception {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setEncounterConfidentiality(Confidentiality.Normal);

        encounterBundle.setPatientConfidentiality(Confidentiality.Unrestricted);
        assertFalse(isConfidentialPatient(asList(encounterBundle)));

        encounterBundle.setPatientConfidentiality(Confidentiality.Low);
        assertFalse(isConfidentialPatient(asList(encounterBundle)));

        encounterBundle.setPatientConfidentiality(Confidentiality.Moderate);
        assertFalse(isConfidentialPatient(asList(encounterBundle)));

        encounterBundle.setPatientConfidentiality(Confidentiality.Normal);
        assertFalse(isConfidentialPatient(asList(encounterBundle)));

        encounterBundle.setPatientConfidentiality(Confidentiality.Restricted);
        assertTrue(isConfidentialPatient(asList(encounterBundle)));

        encounterBundle.setPatientConfidentiality(Confidentiality.VeryRestricted);
        assertTrue(isConfidentialPatient(asList(encounterBundle)));
    }

    @Test
    public void shouldFilterListOfEncounters() throws Exception {
        EncounterBundle encounterBundle1 = new EncounterBundle();
        encounterBundle1.setPatientConfidentiality(Confidentiality.Normal);
        encounterBundle1.setEncounterConfidentiality(Confidentiality.Normal);

        EncounterBundle encounterBundle2 = new EncounterBundle();
        encounterBundle2.setPatientConfidentiality(Confidentiality.Restricted);
        encounterBundle2.setEncounterConfidentiality(Confidentiality.Normal);

        EncounterBundle encounterBundle3 = new EncounterBundle();
        encounterBundle3.setPatientConfidentiality(Confidentiality.Normal);
        encounterBundle3.setEncounterConfidentiality(Confidentiality.Restricted);

        assertEquals(3, filterEncounters(false, asList(encounterBundle1, encounterBundle2, encounterBundle3)).size());

        assertEquals(1, filterEncounters(true, asList(encounterBundle1, encounterBundle2, encounterBundle3)).size());
    }
}