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
import java.util.Date;
import java.util.UUID;

@XmlRootElement(name = "encounter")
public class EncounterBundle {
    private String encounterId;
    private String healthId;
    private Date receivedAt;
    private EncounterContent encounterContent;
    private Confidentiality encounterConfidentiality;
    private Confidentiality patientConfidentiality;
    private int contentVersion = 1;
    private Requester createdBy;
    private Requester updatedBy;
    private Date updatedAt;
    private UUID updatedEventReference;
    private UUID receivedEventReference;
    private String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonProperty("id")
    @XmlElement(name = "id")
    public String getEncounterId() {
        return encounterId;
    }

    @XmlElement(name = "healthId")
    public String getHealthId() {
        return healthId;
    }

    @JsonProperty("updatedAt")
    @XmlElement(name = "updatedAt")
    public String getUpdatedDateISOString() {
        return DateUtil.toISOString(updatedAt);
    }

    @JsonProperty("receivedAt")
    @XmlElement(name = "receivedAt")
    public String getReceivedDateISOString() {
        return DateUtil.toISOString(receivedAt);
    }

    @XmlElement(name = "content")
    @XmlCDATA
    public String getContent() {
        return this.encounterContent.toString();
    }

    @JsonIgnore
    @XmlTransient
    public Date getReceivedAt() {
        return receivedAt;
    }
    @JsonIgnore
    @XmlTransient
    public EncounterContent getEncounterContent() {
        return encounterContent;
    }

    @JsonIgnore
    @XmlTransient
    public Confidentiality getEncounterConfidentiality() {
        return encounterConfidentiality;
    }

    @JsonIgnore
    @XmlTransient
    public Confidentiality getPatientConfidentiality() {
        return patientConfidentiality;
    }

    @JsonIgnore
    @XmlTransient
    public int getContentVersion() {
        return contentVersion;
    }

    @JsonIgnore
    @XmlTransient
    public Requester getCreatedBy() {
        return createdBy;
    }

    @JsonIgnore
    @XmlTransient
    public Requester getUpdatedBy() {
        return updatedBy;
    }

    @JsonIgnore
    @XmlTransient
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @JsonIgnore
    @XmlTransient
    public UUID getUpdatedEventReference() {
        return updatedEventReference;
    }


    @JsonIgnore
    @XmlTransient
    public boolean isConfidential() {
        return getConfidentialityLevel().ordinal() > Confidentiality.Normal.ordinal();
    }

    @JsonIgnore
    @XmlTransient
    public boolean isConfidentialPatient() {
        return getPatientConfidentiality().ordinal() > Confidentiality.Normal.ordinal();
    }

    @JsonIgnore
    @XmlTransient
    public Confidentiality getConfidentialityLevel() {
        if(this.getPatientConfidentiality().ordinal() > Confidentiality.Normal.ordinal())
            return this.getPatientConfidentiality();
        return this.getEncounterConfidentiality();
    }

    public void setContentVersion(int contentVersion) {
        this.contentVersion = contentVersion;
    }

    public void setContent(String content) {
        this.encounterContent = new EncounterContent(content);
    }

    public void setEncounterConfidentiality(Confidentiality encounterConfidentiality) {
        this.encounterConfidentiality = encounterConfidentiality;
    }

    public void setPatientConfidentiality(Confidentiality patientConfidentiality) {
        this.patientConfidentiality = patientConfidentiality;
    }

    public void setEncounterContent(String content) {
        this.encounterContent = new EncounterContent(content);
    }

    public void setReceivedAt(Date date) {
        this.receivedAt = date;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public void setCreatedBy(Requester createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(Requester updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedAt(Date updatedDate) {
        this.updatedAt = updatedDate;
    }

    public void setUpdatedEventReference(UUID updatedEventReference) {
        this.updatedEventReference = updatedEventReference;
    }


    @Override
    public String toString() {
        return "EncounterBundle{" +
                "encounterId='" + encounterId + '\'' +
                ", healthId='" + healthId + '\'' +
                ", receivedAt='" + receivedAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", encounterContent=" + encounterContent +
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

    public void setReceivedEventReference(UUID receivedAtUuid) {
        this.receivedEventReference = receivedAtUuid;
    }

    @JsonIgnore
    @XmlTransient
    public UUID getReceivedEventReference() {
        return receivedEventReference;
    }
}
