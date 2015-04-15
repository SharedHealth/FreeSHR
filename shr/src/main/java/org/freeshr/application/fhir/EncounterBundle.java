package org.freeshr.application.fhir;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.persistence.oxm.annotations.XmlCDATA;
import org.freeshr.domain.model.Requester;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.DateUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.Date;

@XmlRootElement(name = "encounter")
public class EncounterBundle {
    @JsonProperty("id")
    private String encounterId;
    private String healthId;
    @JsonIgnore
    @XmlTransient
    private Date receivedAt;

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

    //TODO : change on encounter update.
    @JsonIgnore
    @XmlTransient
    private int contentVersion = 1;

    @JsonIgnore
    @XmlTransient
    private Requester createdBy;

    @JsonIgnore
    @XmlTransient
    private Requester updatedBy;

    @JsonIgnore
    @XmlTransient
    private Date updatedAt;

    @XmlElement(name = "id")
    public String getEncounterId() {
        return encounterId;
    }

    @JsonProperty("publishedDate")
    @XmlElement(name = "updated")
    public String getUpdatedDateISOString() {
        return DateUtil.toISOString(updatedAt);
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

    public void setReceivedAt(Date date) {
        this.receivedAt = date;
    }

    public Date getReceivedAt() {
        return receivedAt;
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

    @JsonProperty("title")
    public String getTitle() {
        return title + ":" + getEncounterId();
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

    public int getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(int contentVersion) {
        this.contentVersion = contentVersion;
    }

    public Requester getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Requester createdBy) {
        this.createdBy = createdBy;
    }

    public Requester getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Requester updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedDate) {
        this.updatedAt = updatedDate;
    }

    @Override
    public String toString() {
        return "EncounterBundle{" +
                "encounterId='" + encounterId + '\'' +
                ", healthId='" + healthId + '\'' +
                ", receivedAt='" + receivedAt + '\'' +
                ", encounterContent=" + encounterContent +
                ", categories=" + Arrays.toString(categories) +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncounterBundle that = (EncounterBundle) o;

        if (receivedAt != null ? !receivedAt.equals(that.receivedAt) : that.receivedAt != null) return false;
        if (encounterId != null ? !encounterId.equals(that.encounterId) : that.encounterId != null) return false;
        if (healthId != null ? !healthId.equals(that.healthId) : that.healthId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = encounterId != null ? encounterId.hashCode() : 0;
        result = 31 * result + (healthId != null ? healthId.hashCode() : 0);
        result = 31 * result + (receivedAt != null ? receivedAt.hashCode() : 0);
        return result;
    }
}
