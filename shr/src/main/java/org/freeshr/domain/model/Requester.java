package org.freeshr.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Requester {
    @JsonProperty("facilityId")
    private String facilityId;
    @JsonProperty("providerId")
    private String providerId;

    public Requester() {
    }

    public Requester(String facilityId, String providerId) {
        this.facilityId = facilityId;
        this.providerId = providerId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getProviderId() {
        return providerId;
    }
}
