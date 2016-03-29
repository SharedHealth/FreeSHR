package org.freeshr.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String SHR_USER_GROUP = ROLE_PREFIX + "SHR User";
    public static final String FACILITY_GROUP = ROLE_PREFIX + "SHR_FACILITY";
    public static final String PROVIDER_GROUP = ROLE_PREFIX + "SHR_PROVIDER";
    public static final String PATIENT_GROUP = ROLE_PREFIX + "SHR_PATIENT";
    public static final String SHR_SYSTEM_ADMIN_GROUP = ROLE_PREFIX + "SHR System Admin";
    public static final String HRM_SHR_USER_GROUP = "SHR User";
    public static final String HRM_FACILITY_ADMIN_GROUP = "Facility Admin";
    public static final String HRM_PROVIDER_GROUP = "Provider";
    public static final String HRM_PATIENT_GROUP = "Patient";
    public static final String HRM_SHR_SYSTEM_ADMIN_GROUP = "SHR System Admin";

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("is_active")
    private int isActive;
    @JsonProperty("activated")
    private boolean activated;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("groups")
    private List<String> groups;
    @JsonProperty("profiles")
    private List<UserProfile> userProfiles;
    @JsonIgnore
    private List<String> userGroups;

    private UserInfoProperties instance;

    public UserInfo(String id, String name, String email, int isActive, boolean activated, String accessToken, List<String> groups, List<UserProfile> userProfiles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
        this.activated = activated;
        this.accessToken = accessToken;
        this.groups = groups;
        this.userProfiles = userProfiles;
        this.userGroups = new ArrayList<>();
    }

    public UserInfo() {
        this.userGroups = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;

        UserInfo userInfo = (UserInfo) o;

        if (activated != userInfo.activated) return false;
        if (isActive != userInfo.isActive) return false;
        if (!accessToken.equals(userInfo.accessToken)) return false;
        if (!email.equals(userInfo.email)) return false;
        if (groups != null ? !groups.equals(userInfo.groups) : userInfo.groups != null) return false;
        if (!id.equals(userInfo.id)) return false;
        if (name != null ? !name.equals(userInfo.name) : userInfo.name != null) return false;
        if (userProfiles != null ? !userProfiles.equals(userInfo.userProfiles) : userInfo.userProfiles != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + email.hashCode();
        result = 31 * result + isActive;
        result = 31 * result + (activated ? 1 : 0);
        result = 31 * result + accessToken.hashCode();
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (userProfiles != null ? userProfiles.hashCode() : 0);
        return result;
    }

    public UserInfoProperties getProperties() {
        if (null == instance) {
            instance = new UserInfoProperties();
        }
        return instance;
    }

    public class UserInfoProperties {
        private boolean isShrSystemAdmin;
        private String facilityId;
        private String providerId;
        private String patientHid;

        public UserInfoProperties() {
            loadUserProperties();
        }

        public String getName() {
            return name;
        }

        public List<String> getUserGroups() {
            return userGroups;
        }

        public String getId() {
            return id;
        }

        public List<UserProfile> getUserProfiles() {
            return userProfiles;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public boolean isActivated() {
            return activated;
        }

        public int getIsActive() {
            return isActive;
        }

        public String getEmail() {
            return email;
        }

        public String getFacilityId() {
            return facilityId;
        }

        public String getProviderId() {
            return providerId;
        }

        public String getPatientHid() {
            return patientHid;
        }

        public boolean hasCatchmentForProfileType(String requestedCatchment, List<String> profileTypes) {
            for (String profileType : profileTypes) {
                UserProfile userProfile = getUserProfileByType(profileType);
                if (userProfile != null && userProfile.hasCatchment(requestedCatchment)) {
                    return true;
                }
            }
            return false;
        }

        private UserProfile getUserProfileByType(String profileType) {
            if (!isEmpty(userProfiles)) {
                for (UserProfile userProfile : userProfiles) {
                    if (userProfile.getName().equalsIgnoreCase(profileType)) {
                        return userProfile;
                    }
                }
            }
            return null;
        }

        public void loadUserProperties() {
            addDefaultUserGroups();
            if (containsCaseInsensitive(groups, HRM_SHR_USER_GROUP)) {
                loadUserGroupsAndPropertiesBasedOnProfiles();
            }
            if (containsCaseInsensitive(userGroups, SHR_SYSTEM_ADMIN_GROUP)) {
                isShrSystemAdmin = true;
            }
        }

        private boolean containsCaseInsensitive(List<String> groups, String group) {
            for (String groupMember : groups) {
                if (groupMember.equalsIgnoreCase(group)) {
                    return true;
                }
            }
            return false;
        }

        private void loadUserGroupsAndPropertiesBasedOnProfiles() {
            if (isEmpty(userProfiles)) return;
            for (UserProfile userProfile : userProfiles) {
                addGroupsBasedOnProfiles(userProfile);
                loadFacilityProperties(userProfile);
                loadProviderProperties(userProfile);
                loadPatientProperties(userProfile);
            }
        }

        private void addDefaultUserGroups() {
            if (containsCaseInsensitive(groups, HRM_SHR_USER_GROUP)) {
                userGroups.add(SHR_USER_GROUP);
            }
            if (containsCaseInsensitive(groups, HRM_SHR_SYSTEM_ADMIN_GROUP)) {
                userGroups.add(SHR_SYSTEM_ADMIN_GROUP);
            }
        }

        private void addGroupsBasedOnProfiles(UserProfile userProfile) {
            if (userProfile.isFacility() && containsCaseInsensitive(groups, HRM_FACILITY_ADMIN_GROUP)
                    && !containsCaseInsensitive(groups, HRM_SHR_SYSTEM_ADMIN_GROUP)) {
                userGroups.add(FACILITY_GROUP);
            } else if (userProfile.isProvider() && containsCaseInsensitive(groups, HRM_PROVIDER_GROUP)) {
                userGroups.add(PROVIDER_GROUP);
            } else if (userProfile.isPatient() && containsCaseInsensitive(groups, HRM_PATIENT_GROUP)) {
                userGroups.add(PATIENT_GROUP);
            }
        }

        private void loadPatientProperties(UserProfile userProfile) {
            if (userProfile.isPatient()) {
                patientHid = userProfile.getId();
            }
        }

        private void loadProviderProperties(UserProfile userProfile) {
            if (userProfile.isProvider()) {
                providerId = userProfile.getId();
            }
        }

        private void loadFacilityProperties(UserProfile userProfile) {
            if (userProfile.isFacility()) {
                facilityId = userProfile.getId();
            }
        }

        public boolean isShrSystemAdmin() {
            return isShrSystemAdmin;
        }
    }
}
