package org.freeshr.domain.model;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.freeshr.domain.model.patient.Address;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Facility {

    private String facilityId;

    private String facilityName;

    private List<String> catchments = new ArrayList<>();

    private String facilityType;

    private Address facilityLocation;

    public String getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public Address getFacilityLocation() {
        return facilityLocation;
    }

    public void setFacilityLocation(Address facilityLocation) {
        this.facilityLocation = facilityLocation;
    }

    public void setCatchments(String commaSeparatedCatchments) {
        String[] catchments = commaSeparatedCatchments.split(",");
        for (String catchment : catchments) {
            this.catchments.add(catchment.trim());
        }
    }

    public Facility(){}

    //Only for tests
    public Facility(String facilityId, String facilityName, String facilityType, String catchments, Address location) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.facilityType = facilityType;
        this.setFacilityLocation(location);
        setCatchments(catchments);
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }


    public String getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public String getCatchmentsAsCommaSeparatedString() {
        return join(catchments, ",");
    }

    public List<String> getCatchments() {
        return this.catchments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Facility facility = (Facility) o;

        if (!catchments.equals(facility.catchments)) return false;
        if (!facilityId.equals(facility.facilityId)) return false;
        if (!facilityLocation.equals(facility.facilityLocation)) return false;
        if (!facilityName.equals(facility.facilityName)) return false;
        if (!facilityType.equals(facility.facilityType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = facilityId.hashCode();
        result = 31 * result + facilityName.hashCode();
        result = 31 * result + catchments.hashCode();
        result = 31 * result + facilityType.hashCode();
        result = 31 * result + facilityLocation.hashCode();
        return result;
    }

    public boolean has(String catchment) {
        for (String facilityCatchment : catchments) {
            boolean isPartOfFacilitiesCatchment = catchment.startsWith(facilityCatchment);
            if (isPartOfFacilitiesCatchment) return true;
        }
        //return getCatchments().contains(catchment);
        return false;
    }
}
