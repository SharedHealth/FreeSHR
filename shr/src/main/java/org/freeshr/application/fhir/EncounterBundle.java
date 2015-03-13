package org.freeshr.application.fhir;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.persistence.oxm.annotations.XmlCDATA;
import org.freeshr.utils.Confidentiality;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;

@XmlRootElement(name = "encounter")
public class EncounterBundle {

    @JsonProperty("id")
    private String encounterId;


    private String healthId;

    @JsonProperty("publishedDate")
    private String receivedDate;

    @JsonIgnore
    @XmlTransient
    private EncounterContent encounterContent;

    private String[] categories = new String[]{"encounter"};

    private String title = "Encounter";

    @JsonIgnore
    @XmlTransient
    private Confidentiality encounterConfidentiality;

    @JsonIgnore
    @XmlTransient
    private Confidentiality patientConfidentiality;

    public void setEncounterContent(EncounterContent encounterContent) {
        this.encounterContent = encounterContent;
    }

    @XmlElement(name = "id")
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

    @XmlElement(name = "updated")
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

    @XmlTransient
    public EncounterContent getEncounterContent() {
        return encounterContent;
    }

    public void setEncounterContent(String content) {
        this.encounterContent = new EncounterContent(content);
    }

    @JsonProperty("content")
    @XmlElement(name = "content")
    @XmlCDATA
    public String getContent() {
        return this.encounterContent.toString();
    }

    @JsonProperty("link")
    @XmlElement(name = "link")
    public String getLink() {
        return String.format("/patients/%s/encounters/%s", getHealthId(), getEncounterId());
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }


    public String[] getCategories() {
        return categories != null ? categories : new String[]{};
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title + ":" + getEncounterId();
    }

    @Override
    public String toString() {
        return "EncounterBundle{" +
                "encounterId='" + encounterId + '\'' +
                ", healthId='" + healthId + '\'' +
                ", receivedDate='" + receivedDate + '\'' +
                ", encounterContent=" + encounterContent +
                ", categories=" + Arrays.toString(categories) +
                ", title='" + title + '\'' +
                '}';
    }

    public Confidentiality getEncounterConfidentiality() {
        return encounterConfidentiality;
    }

    public void setEncounterConfidentiality(Confidentiality encounterConfidentiality) {
        this.encounterConfidentiality = encounterConfidentiality;
    }

    public void setPatientConfidentiality(Confidentiality patientConfidentiality) {
        this.patientConfidentiality = patientConfidentiality;
    }

    public Confidentiality getPatientConfidentiality() {
        return patientConfidentiality;
    }
}