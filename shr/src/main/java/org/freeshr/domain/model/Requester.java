package org.freeshr.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Requester)) return false;

        Requester requester = (Requester) o;

        if (facilityId != null ? !facilityId.equals(requester.facilityId) : requester.facilityId != null) return false;
        if (providerId != null ? !providerId.equals(requester.providerId) : requester.providerId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = facilityId != null ? facilityId.hashCode() : 0;
        result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
