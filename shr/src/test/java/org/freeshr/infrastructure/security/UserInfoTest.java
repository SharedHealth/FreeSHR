package org.freeshr.infrastructure.security;

import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserInfo.FACILITY_ADMIN_GROUP;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserInfoTest {
    @Test
    public void shouldAddFacilityGroupIfUserProfileHasFacilityAndGroupHasFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile("facility", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(FACILITY_ADMIN_GROUP)), asList(userProfile));
        userInfo.loadUserProperties();
        assertTrue(userInfo.getGroups().contains(UserInfo.SHR_FACILITY_GROUP));
    }

    @Test
    public void shouldNotAddFacilityGroupIfUserProfileNotHaveFacilityAndGroupNotHaveFacilityAdmin() throws Exception {
        UserProfile userProfile = new UserProfile("facility", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
        userInfo.loadUserProperties();
        assertFalse(userInfo.getGroups().contains(UserInfo.SHR_FACILITY_GROUP));

        new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<>(asList(FACILITY_ADMIN_GROUP)), null);
        userInfo.loadUserProperties();
        assertFalse(userInfo.getGroups().contains(UserInfo.SHR_FACILITY_GROUP));
    }

    @Test
    public void shouldAddProviderGroupOnlyIfUserProfileHasProvider() throws Exception {
        UserProfile userProfile = new UserProfile("provider", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
        userInfo.loadUserProperties();
        assertTrue(userInfo.getGroups().contains(UserInfo.SHR_PROVIDER_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), null);
        userInfo.loadUserProperties();
        assertFalse(userInfo.getGroups().contains(UserInfo.SHR_PROVIDER_GROUP));
    }

    @Test
    public void shouldAddPatientGroupOnlyIfUserProfileHasPatient() throws Exception {
        UserProfile userProfile = new UserProfile("patient", "100067", null);

        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), asList(userProfile));
        userInfo.loadUserProperties();
        assertTrue(userInfo.getGroups().contains(UserInfo.SHR_PATIENT_GROUP));

        userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
                new ArrayList<String>(), null);
        userInfo.loadUserProperties();
        assertFalse(userInfo.getGroups().contains(UserInfo.SHR_PATIENT_GROUP));
    }
}