package org.freeshr.infrastructure.security;

import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserInfo.FACILITY_ADMIN_GROUP;
import static org.freeshr.infrastructure.security.UserInfo.FACILITY_GROUP;
import static org.freeshr.infrastructure.security.UserInfo.SHR_USER_GROUP;
import static org.freeshr.infrastructure.security.UserProfile.FACILITY_TYPE;
import static org.freeshr.infrastructure.security.UserProfile.PATIENT_TYPE;
import static org.freeshr.infrastructure.security.UserProfile.PROVIDER_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserInfoTest {

    @Test
    public void shouldNotAddUserGroupsIfItDoesNotHaveShrUserGroup() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(FACILITY_ADMIN_GROUP)), asList(userProfile));
        assertFalse(userInfo.getProperties().getGroups().contains(FACILITY_GROUP));
    }

    @Test
    public void shouldAddFacilityGroupIfUserProfileHasFacilityAndGroupHasFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(SHR_USER_GROUP, FACILITY_ADMIN_GROUP)), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(UserInfo.FACILITY_GROUP));
    }

    @Test
    public void shouldNotAddFacilityGroupIfUserProfileNotHaveFacilityAndGroupNotHaveFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile(FACILITY_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(SHR_USER_GROUP)), asList(userProfile));
        assertFalse(userInfo.getProperties().getGroups().contains(UserInfo.FACILITY_GROUP));

        new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(SHR_USER_GROUP, FACILITY_ADMIN_GROUP)), null);
        assertFalse(userInfo.getProperties().getGroups().contains(UserInfo.FACILITY_GROUP));
    }

    @Test
    public void shouldAddProviderGroupOnlyIfUserProfileHasProvider() throws Exception {
        UserProfile userProfile = new UserProfile(PROVIDER_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(SHR_USER_GROUP)), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(UserInfo.PROVIDER_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(SHR_USER_GROUP)), null);
        assertFalse(userInfo.getProperties().getGroups().contains(UserInfo.PROVIDER_GROUP));
    }

    @Test
    public void shouldAddPatientGroupOnlyIfUserProfileHasPatient() throws Exception {
        UserProfile userProfile = new UserProfile(PATIENT_TYPE, "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(SHR_USER_GROUP)), asList(userProfile));
        assertTrue(userInfo.getProperties().getGroups().contains(UserInfo.PATIENT_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(SHR_USER_GROUP)), null);
        assertFalse(userInfo.getProperties().getGroups().contains(UserInfo.PATIENT_GROUP));
    }

    @Test
    public void shouldCheckCatchmentsForGivenType() throws Exception {
        UserProfile patientProfile = new UserProfile(PATIENT_TYPE, "100067", null);
        UserProfile facilityProfile = new UserProfile(FACILITY_TYPE, "100067", asList("3026", "4019"));
        UserProfile providerProfile = new UserProfile(PROVIDER_TYPE, "100067", asList("1001"));

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(providerProfile, facilityProfile, patientProfile));

        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("3026", asList(FACILITY_TYPE)));
        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("4019", asList(FACILITY_TYPE)));
        assertFalse(userInfo.getProperties().hasCatchmentForProfileType("1001", asList(FACILITY_TYPE)));

        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("1001", asList(PROVIDER_TYPE)));
        assertFalse(userInfo.getProperties().hasCatchmentForProfileType("3026", asList(PROVIDER_TYPE)));

        assertFalse(userInfo.getProperties().hasCatchmentForProfileType("2020", asList(PATIENT_TYPE)));

        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("4019", asList(PROVIDER_TYPE, FACILITY_TYPE)));
        assertTrue(userInfo.getProperties().hasCatchmentForProfileType("3026", asList(PROVIDER_TYPE, FACILITY_TYPE)));
    }
}