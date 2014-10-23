package org.freeshr.domain.model;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Catchment {
    ////division_id, district_id, upazila_id, city_corporation_id, union_urban_ward_id
    public static final String DIVISION_ID = "division_id";
    public static final String DISTRICT_ID = "district_id";
    public static final String UPAZILA_ID = "upazila_id";
    public static final String CITY_CORPORATION_ID = "city_corporation_id";
    public static final String UNION_OR_URBAN_WARD_ID = "union_urban_ward_id";


    private static final Map<Integer, String> catchmentTypes = new HashMap<Integer, String>() {{
        put(1, DIVISION_ID);
        put(2, DISTRICT_ID);
        put(3, UPAZILA_ID);
        put(4, CITY_CORPORATION_ID);
        put(5, UNION_OR_URBAN_WARD_ID);
    }};

    private String code;
    private boolean valid = false;
    private int level = 0;

    public Catchment(String code) {
        parse(code);
    }

    public String getCode() {
        return this.code;
    }

    public String getType() {
        return catchmentTypes.get(this.level);
    }

    private void parse(String catchment) {
        int mod = catchment.length() % 2;
        if (mod != 0) return;

        this.code = catchment;
        this.level = catchment.length()/2;
        this.valid = this.level > 0;
    }

    public int getLevel() {
        return level;
    }

    public String levelCode(int i) {
        if (i > this.level) return null;
        if (StringUtils.isBlank(this.code)) return null;
        
        return code.substring(0, i*2);
    }

    public String levelType(int level) {
        if (level > this.level) return null;
        return catchmentTypes.get(level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Catchment)) return false;

        Catchment that = (Catchment) o;

        if (level != that.level) return false;
        if (valid != that.valid) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (valid ? 1 : 0);
        result = 31 * result + level;
        return result;
    }

    public boolean isValid() {
        return valid;
    }
}
