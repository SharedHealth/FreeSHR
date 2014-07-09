package org.freeshr.application.fhir;


public class EncounterBundle {

    private String encounterId;
    private String healthId;
    private String date;
    private String content;

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EncounterBundle{");
        sb.append("encounterId='").append(encounterId).append('\'');
        sb.append(", healthId='").append(healthId).append('\'');
        sb.append(", date='").append(date).append('\'');
        //sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
