package org.freeshr.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {
    private static final String FACILITY_TYPE = "facility";
    private static final String PROVIDER_TYPE = "provider";
    private static final String PATIENT_TYPE = "patient";
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private String id;
    @JsonProperty("catchment")
    private List<String> catchments;

    public UserProfile() {
    }

    public UserProfile(String name, String id, List<String> catchments) {
        this.name = name;
        this.id = id;
        this.catchments = catchments;
    }

    public boolean isFaciltiyType() {
        return name.equalsIgnoreCase(FACILITY_TYPE);
    }

    public boolean isProviderType() {
        return name.equalsIgnoreCase(PROVIDER_TYPE);
    }

    public boolean isPatientType() {
        return name.equalsIgnoreCase(PATIENT_TYPE);
    }
}
