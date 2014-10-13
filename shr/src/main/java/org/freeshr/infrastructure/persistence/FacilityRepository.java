package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.join;
import static org.freeshr.utils.CollectionUtils.map;

@Component
public class FacilityRepository {
    private CqlOperations cqlOperations;

    @Autowired
    public FacilityRepository(@Qualifier("SHRCassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public Facility find(String facilityId) throws ExecutionException {
        ResultSet resultSet = cqlOperations.query("SELECT * FROM facilities WHERE facility_id='" + facilityId + "';");
        return read(resultSet);
    }

    private Facility read(ResultSet resultSet) throws ExecutionException {
        Row result = resultSet.one();
        if (null != result) {
            Facility facility = new Facility();
            facility.setFacilityId(result.getString("facility_id"));
            facility.setFacilityName(result.getString("facility_name"));
            facility.setFacilityType(result.getString("facility_type"));
            facility.setCatchments(result.getString("catchments"));
            Address address = new Address();
            address.setDivision(result.getString("division_id"));
            address.setDistrict(result.getString("district_id"));
            address.setUpazilla(result.getString("upazilla_id"));
            address.setCityCorporation(result.getString("city_corporation_id"));
            address.setWard(result.getString("ward_id"));
            facility.setFacilityLocation(address);
            return facility;
        } else {
            return null;
        }
    }
    public void save(Facility facility) {
        cqlOperations.execute(toCQL(facility));
    }

    private String toCQL(Facility facility) {
        String query = query(asList(facility.getFacilityId(), facility.getFacilityName(), facility.getFacilityType(),
                facility.getFacilityLocation().getDivision(), facility.getFacilityLocation().getDistrict(), facility.getFacilityLocation().getUpazilla(), facility.getFacilityLocation().getCityCorporation(),facility.getFacilityLocation().getWard(),
                facility.getCatchmentsInCommaSeparatedString()));
        return "INSERT into facilities (facility_id, facility_name, facility_type, division_id, district_id, upazilla_id, city_corporation_id,ward_id, catchments) values  (" + query + ")";
    }

    private String query(List<String> values) {
        return join(map(values, new CollectionUtils.Fn<String, String>() {
            public String call(String input) {
                return "'" + input + "'";
            }
        }), ",");
    }

}
