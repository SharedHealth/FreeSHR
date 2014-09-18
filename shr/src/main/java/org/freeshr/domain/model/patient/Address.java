package org.freeshr.domain.model.patient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    @JsonProperty("address_line")
    private String line;

    @JsonProperty("district_id")
    private String district;

    @JsonProperty("division_id")
    private String division;

    @JsonProperty("ward_id")
    private String ward;

    @JsonProperty("upazilla_id")
    private String upazilla;

    @JsonProperty("city_corporation_id")
    private String cityCorporation;

    public Address(){

    }

    public Address(String division, String district, String upazilla, String cityCorporation, String ward) {
        this.division = division;
        this.district = district;
        this.upazilla = upazilla;
        this.cityCorporation = cityCorporation;
        this.ward = ward;
    }


    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getUpazilla() {
        return upazilla;
    }

    public void setUpazilla(String upazilla) {
        this.upazilla = upazilla;
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getCityCorporation() {
        return cityCorporation;
    }

    public void setCityCorporation(String cityCorporation) {
        this.cityCorporation = cityCorporation;
    }
}
