package org.freeshr.domain.model.patient;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {

    @JsonProperty("hid")
    private String healthId;

    @JsonProperty("present_address")
    private Address address;

    @JsonProperty("gender")
    private String gender;

    private boolean isConfidential;

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

    public void setGender(String gender) {
        this.gender = gender;
    }


    public boolean isConfidential() {
        return isConfidential;
    }

    //this is bad. done this way because MCI chooses to send booleans as yes/no
    public void setConfidential(boolean isConfidential) {
        this.isConfidential = isConfidential ;
    }

    @JsonProperty("confidential")
    public void setConfidential(String confidential) {
        this.isConfidential = "YES".equalsIgnoreCase(confidential);
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
