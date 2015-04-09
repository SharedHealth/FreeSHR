package org.freeshr.domain.model;

public class Requester {
    private String facilityId;
    private String providerId;

    public Requester(String facilityId, String providerId) {
        this.facilityId = facilityId;
        this.providerId = providerId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Requester{");
        sb.append("facilityId=").append(facilityId);
        sb.append(", providerId=").append(providerId);
        sb.append('}');
        return sb.toString();
    }
}
