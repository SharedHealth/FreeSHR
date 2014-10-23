package org.freeshr.application.fhir;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://www.w3.org/2005/Atom", name = "feed")
public class EncounterBundle {

    private String encounterId;
    private String healthId;
    private String receivedDate;

    @JsonIgnore
    private EncounterContent encounterContent;

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

    public String getReceivedDate() {
        return receivedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncounterBundle that = (EncounterBundle) o;

        if (receivedDate != null ? !receivedDate.equals(that.receivedDate) : that.receivedDate != null) return false;
        if (encounterId != null ? !encounterId.equals(that.encounterId) : that.encounterId != null) return false;
        if (healthId != null ? !healthId.equals(that.healthId) : that.healthId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = encounterId != null ? encounterId.hashCode() : 0;
        result = 31 * result + (healthId != null ? healthId.hashCode() : 0);
        result = 31 * result + (receivedDate != null ? receivedDate.hashCode() : 0);
        return result;
    }

    public void setReceivedDate(String date) {
        this.receivedDate = date;
    }

    public EncounterContent getEncounterContent() {
        return encounterContent;
    }

    public void setEncounterContent(String content) {
        this.encounterContent = new EncounterContent(content);
    }

    @JsonProperty("content")
    public String getContent() {
        return this.encounterContent.toString();
    }
}
