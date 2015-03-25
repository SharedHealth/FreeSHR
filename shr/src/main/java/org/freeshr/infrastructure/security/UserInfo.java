package org.freeshr.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    public static final String SHR_FACILITY_GROUP = "SHR_FACILITY";
    public static final String SHR_USER_GROUP = "SHR USER";
    public static final String SHR_PROVIDER_GROUP = "SHR_PROVIDER";
    public static final String SHR_PATIENT_GROUP = "SHR_PATIENT";
    public static final String FACILITY_ADMIN_GROUP = "Facility Admin";
    public static final String DATASENSE_FACILITY_GROUP = "Datasense Facility";

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

    private boolean isDatasenseFacility;
    private String facilityId;
    private String providerId;
    private String patientHid;
    private List<String> catchments;

    public UserInfo() {
    }

    public UserInfo(String id, String name, String email, int isActive, boolean activated, String accessToken, List<String> groups,
                    List<UserProfile> userProfiles) {
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

    public String getFacilityId() {
        return facilityId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getPatientHid() {
        return patientHid;
    }

    public boolean isNotDatasenseFacility() {
        return !isDatasenseFacility;
    }

    public List<String> getCatchments() {
        return catchments;
    }

    public boolean hasCatchment(String requestedCatchment) {
        for (String catchment : catchments) {
            if (requestedCatchment.startsWith(catchment))
                return true;
        }
        return false;
    }

    //TODO: This pattern of having clients of this class to loadUserProperties explicitly
    // is highly error prone, we should take utmost care to make sure objects are valid on construction.
    public UserInfo loadUserProperties() {
        catchments = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userProfiles)) {
            for (UserProfile userProfile : userProfiles) {
                addGroupsBasedOnProfiles(userProfile);
                loadFacilityProperties(userProfile);
                loadProviderProperties(userProfile);
                loadPatientProperties(userProfile);
            }
        }
        if (groups.contains(DATASENSE_FACILITY_GROUP)) {
            isDatasenseFacility = true;
        }
        return this;
    }

    private void addGroupsBasedOnProfiles(UserProfile userProfile) {
        if (userProfile.isFacility() && groups.contains(FACILITY_ADMIN_GROUP)
                && !groups.contains(DATASENSE_FACILITY_GROUP)) {
            groups.add(SHR_FACILITY_GROUP);
        } else if (userProfile.isProvider()) {
            groups.add(SHR_PROVIDER_GROUP);
        } else if (userProfile.isPatient()) {
            groups.add(SHR_PATIENT_GROUP);
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
            if (CollectionUtils.isNotEmpty(userProfile.getCatchments())) {
                catchments.addAll(userProfile.getCatchments());
            }
        }
    }

    private void loadFacilityProperties(UserProfile userProfile) {
        if (userProfile.isFacility()) {
            facilityId = userProfile.getId();
            if (CollectionUtils.isNotEmpty(userProfile.getCatchments())) {
                catchments.addAll(userProfile.getCatchments());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;

        UserInfo userInfo = (UserInfo) o;

        if (activated != userInfo.activated) return false;
        if (isActive != userInfo.isActive) return false;
        if (isDatasenseFacility != userInfo.isDatasenseFacility) return false;
        if (!accessToken.equals(userInfo.accessToken)) return false;
        if (catchments != null ? !catchments.equals(userInfo.catchments) : userInfo.catchments != null) return false;
        if (!email.equals(userInfo.email)) return false;
        if (facilityId != null ? !facilityId.equals(userInfo.facilityId) : userInfo.facilityId != null) return false;
        if (groups != null ? !groups.equals(userInfo.groups) : userInfo.groups != null) return false;
        if (!id.equals(userInfo.id)) return false;
        if (name != null ? !name.equals(userInfo.name) : userInfo.name != null) return false;
        if (patientHid != null ? !patientHid.equals(userInfo.patientHid) : userInfo.patientHid != null) return false;
        if (providerId != null ? !providerId.equals(userInfo.providerId) : userInfo.providerId != null) return false;
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
        result = 31 * result + (isDatasenseFacility ? 1 : 0);
        result = 31 * result + (facilityId != null ? facilityId.hashCode() : 0);
        result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
        result = 31 * result + (patientHid != null ? patientHid.hashCode() : 0);
        result = 31 * result + (catchments != null ? catchments.hashCode() : 0);
        return result;
    }
}
