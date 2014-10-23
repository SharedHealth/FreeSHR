package org.freeshr.domain.service;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class FacilityCatchment {
    ////division_id, district_id, upazila_id, city_corporation_id, union_urban_ward_id
    public static final String DIVISION_ID = "division_id";
    public static final String DISTRICT_ID = "district_id";
    public static final String UPAZILA_ID = "upazila_id";
    public static final String CITY_CORPORATION_ID = "city_corporation_id";
    public static final String UNION_OR_URBAN_WARD_ID = "union_urban_ward_id";


    private Map<Integer, String> addressLevelMap = new HashMap<Integer, String>() {{
        put(1, DIVISION_ID);
        put(2, DISTRICT_ID);
        put(3, UPAZILA_ID);
        put(4, CITY_CORPORATION_ID);
        put(5, UNION_OR_URBAN_WARD_ID);
    }};

    private String catchment;
    private boolean valid = false;
    private int level = 0;

    public FacilityCatchment(String catchment) {
        parse(catchment);
    }

    public String getCatchment() {
        return this.catchment;
    }

    public String getCatchmentType() {
        return addressLevelMap.get(this.level);
    }

    public String getDivisionId() {
        return null;
    }


    private void parse(String catchment) {
        int mod = catchment.length() % 2;
        if (mod != 0) return;

        this.valid = true;
        this.catchment = catchment;
        this.level = catchment.length()/2;
    }

    public int getLevel() {
        return level;
    }

    public String levelCode(int i) {
        if (i > this.level) return null;
        if (StringUtils.isBlank(this.catchment)) return null;
        
        return catchment.substring(0, i*2);
    }

    public String levelType(int level) {
        if (level > this.level) return null;
        return addressLevelMap.get(level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacilityCatchment)) return false;

        FacilityCatchment that = (FacilityCatchment) o;

        if (level != that.level) return false;
        if (valid != that.valid) return false;
        if (catchment != null ? !catchment.equals(that.catchment) : that.catchment != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = catchment != null ? catchment.hashCode() : 0;
        result = 31 * result + (valid ? 1 : 0);
        result = 31 * result + level;
        return result;
    }
}
