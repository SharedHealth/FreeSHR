package org.freeshr.events;

import java.util.UUID;

public class EncounterEventLog {
    private String encounterId;
    private UUID createdAt;
    private UUID mergedAt;

    public String getEncounterId() {
        return encounterId;
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public void setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(UUID mergedAt) {
        this.mergedAt = mergedAt;
    }
}
