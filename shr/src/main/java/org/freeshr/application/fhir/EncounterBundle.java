package org.freeshr.application.fhir;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class EncounterBundle {

    private String encounterId;
    private String healthId;
    private String date;
    private JsonNode content;

    @JsonIgnore
    private static ObjectMapper mapper = new ObjectMapper();

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

    public JsonNode getContent() {
        return content;
    }

    public void setContent(String content) {
        try {
            this.content = mapper.readTree(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
