package org.freeshr.domain.model.patient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
<<<<<<< HEAD
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
=======
>>>>>>> Sanjana,Dubey | # | Pulled out db migrations from server start to a separate module.
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    @JsonProperty("address_line")
    private String line;

    @JsonProperty("district_id")
    private String district;

    @JsonProperty("division_id")
    private String division;

    @JsonProperty("upazila_id")
    private String upazila;

    @JsonProperty("city_corporation_id")
    private String cityCorporation;

    @JsonProperty("union_or_urban_ward_id")
    private String unionOrUrbanWardId;

    @JsonProperty("country_code")
    private String countryCode;


    public Address() {

    }

    public Address(String division, String district, String upazila, String cityCorporation, String unionOrUrbanWardId) {
        this.division = division;
        this.district = district;
        this.upazila = upazila;
        this.cityCorporation = cityCorporation;
        this.unionOrUrbanWardId = unionOrUrbanWardId;
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

    public String getUnionOrUrbanWardId() {
        return unionOrUrbanWardId;
    }

    public void setUnionOrUrbanWardId(String unionOrUrbanWardId) {
        this.unionOrUrbanWardId = unionOrUrbanWardId;
    }

    public String getUpazila() {
        return upazila;
    }

    public void setUpazila(String upazila) {
        this.upazila = upazila;
    }

    public String getConcatenatedDistrictId() {
        return division + district;
    }

    public String getConcatenatedUpazilaId() {
        return division + district + upazila;
    }

    public String getConcatenatedCityCorporationId() {
        return cityCorporation != null ? division + district + upazila + cityCorporation : null;
    }

    public String getConcatenatedWardId() {
        return unionOrUrbanWardId != null ? division + district + upazila + cityCorporation + unionOrUrbanWardId : null;
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

    public String getLocationCode() {
        return division + district + upazila + StringUtils.defaultString(cityCorporation) + StringUtils.defaultString
                (unionOrUrbanWardId);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "line='" + line + '\'' +
                ", district='" + district + '\'' +
                ", division='" + division + '\'' +
                ", upazila='" + upazila + '\'' +
                ", cityCorporation='" + cityCorporation + '\'' +
                ", unionOrUrbanWardId='" + unionOrUrbanWardId + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }
}
