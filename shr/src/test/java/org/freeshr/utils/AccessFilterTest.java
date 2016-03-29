package org.freeshr.utils;

import org.freeshr.config.SHRProperties;
import org.freeshr.infrastructure.security.AccessFilter;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserInfo.HRM_FACILITY_ADMIN_GROUP;
import static org.freeshr.infrastructure.security.UserInfo.HRM_SHR_SYSTEM_ADMIN_GROUP;
import static org.freeshr.infrastructure.security.UserInfo.HRM_SHR_USER_GROUP;
import static org.freeshr.infrastructure.security.UserInfo.SHR_SYSTEM_ADMIN_GROUP;
import static org.freeshr.infrastructure.security.UserProfile.FACILITY_TYPE;
import static org.freeshr.infrastructure.security.UserProfile.PATIENT_TYPE;
import static org.freeshr.infrastructure.security.UserProfile.PROVIDER_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

    private AccessFilter accessFilter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        accessFilter = new AccessFilter();
        facility = new UserInfo("1", "bahmni", "bahmni@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(HRM_FACILITY_ADMIN_GROUP, HRM_SHR_USER_GROUP)),
                asList(new UserProfile(FACILITY_TYPE, FACILITY_ID, asList(FACILITY_CATCHMENT))));
        datasenseFacility = new UserInfo("2", "datasense", "datasense@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(HRM_FACILITY_ADMIN_GROUP, HRM_SHR_SYSTEM_ADMIN_GROUP)),
                asList(new UserProfile(FACILITY_TYPE, DATASENSE_FACILITY_ID, asList(FACILITY_CATCHMENT))));
        facilityAndProvider = new UserInfo("3", "facilityandprovider", "facilityandprovider@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList("Facility Admin", HRM_SHR_USER_GROUP)),
                asList(new UserProfile(FACILITY_TYPE, FACILITY_ID, asList(FACILITY_CATCHMENT)),
                        new UserProfile("provider", PROVIDER_ID, asList(PROVIDER_CATCHMENT))));
        patient = new UserInfo("4", "patient", "patient@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(HRM_SHR_USER_GROUP)),
                asList(new UserProfile(PATIENT_TYPE, HEALTH_ID, null)));
        providerAndPatient = new UserInfo("5", "providerAndPatient", "providerAndPatient@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(HRM_SHR_USER_GROUP)),
                asList(new UserProfile(PROVIDER_TYPE, PROVIDER_ID, asList(PROVIDER_CATCHMENT)),
                        new UserProfile(PATIENT_TYPE, HEALTH_ID_FOR_PROVIDER, null)));
        provider = new UserInfo("6", "provider", "provider@gmail.com", 1,
                true, "xyz", new ArrayList<>(asList(HRM_SHR_USER_GROUP)),
                asList(new UserProfile(PROVIDER_TYPE, PROVIDER_ID, asList(PROVIDER_CATCHMENT))));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToFacility() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForPatient("some health id", facility));
    }

    @Test
    public void shouldAllowUnRestrictedAccessForEncounterFetchToDatasense() throws Exception {
        assertFalse(accessFilter.isAccessRestrictedToEncounterFetchForPatient("some health id", datasenseFacility));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToFacilityWithProvider() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForPatient("some health id", facilityAndProvider));
    }

    @Test
    public void shouldAllowUnRestrictedAccessForEncounterFetchToPatientForAssociatedHid() throws Exception {
        assertFalse(accessFilter.isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID, patient));
    }

    @Test
    public void shouldNotAllowAccessForEncounterFetchToPatientForOtherHid() throws Exception {
        assertNull(accessFilter.isAccessRestrictedToEncounterFetchForPatient("some hid", patient));
    }

    @Test
    public void shouldAllowUnRestrictedAccessForEncounterFetchToProviderWithPatientForAssociatedHid() throws Exception {
        assertFalse(accessFilter.isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID_FOR_PROVIDER, providerAndPatient));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToProviderWithPatientForOtherHid() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID, providerAndPatient));
    }

    @Test
    public void shouldAllowRestrictedAccessForEncounterFetchToProvider() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForPatient(HEALTH_ID, provider));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToFacilityWithAssociatedCatchment() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(FACILITY_CATCHMENT, facility));
    }

    @Test
    public void shouldNotAllowAccessForCatchmentFetchToFacilityWithOtherCatchment() throws Exception {
        assertNull(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, facility));
    }

    @Test
    public void shouldAllowUnRestrictedAccessForCatchmentFetchToDatasenseWithAssociatedCatchment() throws Exception {
        assertFalse(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(FACILITY_CATCHMENT, datasenseFacility));
    }

    @Test
    public void shouldNotAllowAccessForCatchmentFetchToDatasenseForOtherCatchment() throws Exception {
        assertNull(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, datasenseFacility));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToFacilityAndProviderWithFacilityCatchment() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(FACILITY_CATCHMENT, facilityAndProvider));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToFacilityAndProviderWithProviderCatchment() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, facilityAndProvider));
    }

    @Test
    public void shouldNotAllowAccessForCatchmentFetchToFacilityAndProviderWithOtherCatchment() throws Exception {
        assertNull(accessFilter.isAccessRestrictedToEncounterFetchForCatchment("1029", facilityAndProvider));
    }

    @Test
    public void shouldNotAllowAccessForCatchmentFetchToPatient() throws Exception {
        assertNull(accessFilter.isAccessRestrictedToEncounterFetchForCatchment("1029", patient));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToProviderAndPatientWithAssociatedCatchment() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, providerAndPatient));
    }

    @Test
    public void shouldNotAllowAccessForCatchmentFetchToProviderAndPatientWithOtherCatchment() throws Exception {
        assertNull(accessFilter.isAccessRestrictedToEncounterFetchForCatchment("1029", providerAndPatient));
    }

    @Test
    public void shouldAllowRestrictedAccessForCatchmentFetchToProviderWithAssociatedCatchment() throws Exception {
        assertTrue(accessFilter.isAccessRestrictedToEncounterFetchForCatchment(PROVIDER_CATCHMENT, provider));
    }

    @Test
    public void shouldNotAllowAccessForCatchmentFetchToProviderWithOtherCatchment() throws Exception {
        assertNull(accessFilter.isAccessRestrictedToEncounterFetchForCatchment("1029", provider));
    }
}