package org.freeshr.domain.model;


import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.join;

public class Facility {
    private String facilityId;
    private String facilityName;
    private List<String> catchments=new ArrayList<>();

    public Facility(){}

    //Only for tests
    public Facility(String facilityId, String facilityName, String catchments) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        setCatchments(catchments);
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public void setCatchments(List<String> catchments) {
        this.catchments = catchments;
    }

    public void setCatchments(String catchments) {
        String[] split = catchments.split(",");
        for (String s : split) {
            this.catchments.add(s.trim());
        }

    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public List<String> getCatchments() {
        return catchments;
    }

    public String getCatchmentsInCommaSeparatedString() {
        return join(catchments, ",");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Facility facility = (Facility) o;

        if (!catchments.equals(facility.catchments)) return false;
        if (!facilityId.equals(facility.facilityId)) return false;
        if (!facilityName.equals(facility.facilityName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = facilityId.hashCode();
        result = 31 * result + facilityName.hashCode();
        result = 31 * result + catchments.hashCode();
        return result;
    }
}
