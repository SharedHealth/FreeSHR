package org.freeshr.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    public static final String SHR_FACILITY_GROUP = "SHR_FACILITY";
    public static final String SHR_PROVIDER_GROUP = "SHR_PROVIDER";
    public static final String SHR_PATIENT_GROUP = "SHR_PATIENT";
    public static final String FACILITY_ADMIN_GROUP = "Facility Admin";

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

    public UserInfo() {
    }

    public UserInfo(String id, String name, String email, int isActive, boolean activated, String accessToken, List<String> groups, List<UserProfile> userProfiles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
        this.activated = activated;
        this.accessToken = accessToken;
        this.groups = groups;
        this.userProfiles = userProfiles;
    }

    public String getName() {
        return name;
    }

    public List<String> getGroups() {
        ArrayList<String> groups = new ArrayList<>();
        groups.addAll(this.groups);
        groups.addAll(identifyGroupsFromProfiles());
        return groups;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + email.hashCode();
        result = 31 * result + isActive;
        result = 31 * result + (activated ? 1 : 0);
        result = 31 * result + accessToken.hashCode();
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (userProfiles != null ? userProfiles.hashCode() : 0);
        return result;
    }

    public ArrayList<String> identifyGroupsFromProfiles() {
        ArrayList<String> groupsFromProfiles = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userProfiles)) {
            for (UserProfile userProfile : userProfiles) {
                if (userProfile.isFaciltiyType() && groups.contains(FACILITY_ADMIN_GROUP)) {
                    groupsFromProfiles.add(SHR_FACILITY_GROUP);
                } else if (userProfile.isProviderType()) {
                    groupsFromProfiles.add(SHR_PROVIDER_GROUP);
                } else if (userProfile.isPatientType()) {
                    groupsFromProfiles.add(SHR_PATIENT_GROUP);
                }
            }
        }
        return groupsFromProfiles;
    }
}
