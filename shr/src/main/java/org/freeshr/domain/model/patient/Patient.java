package org.freeshr.domain.model.patient;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.utils.Confidentiality;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {

    @JsonProperty("hid")
    private String healthId;

    @JsonProperty("present_address")
    private Address address;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("merged_with")
    private String mergedWith;

    private Confidentiality confidentiality;

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public String getMergedWith() {
        return mergedWith;
    }


    public void setGender(String gender) {
        this.gender = gender;
    }

    public Confidentiality getConfidentiality() {
        return confidentiality;
    }

    //this is bad. done this way because MCI chooses to send booleans as yes/no
    public void setConfidentiality(boolean isConfidential) {
        this.confidentiality = isConfidential ? Confidentiality.VeryRestricted : Confidentiality.Normal;
    }

    @JsonProperty("confidential")
    public void setConfidentiality(String confidentiality) {
        this.confidentiality = "YES".equalsIgnoreCase(confidentiality) ?
                Confidentiality.VeryRestricted : Confidentiality.Normal;
    }

    public void setMergedWith(String mergedWith) {
        this.mergedWith = mergedWith;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Patient patient = (Patient) o;

        return this.healthId.equals(patient.healthId);
    }

    @Override
    public int hashCode() {
        return healthId.hashCode();
    }

    @Override
    public String toString() {
        return "Patient{" +
                "healthId='" + healthId + '\'' +
                ", address=" + address +
                ", gender='" + gender + '\'' +
                '}';
    }
}
