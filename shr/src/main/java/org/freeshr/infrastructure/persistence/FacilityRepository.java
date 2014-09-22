package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.domain.model.Facility;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.concurrent.SimpleListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

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

    public ListenableFuture<Facility> find(String facilityId) {
        return new SimpleListenableFuture<Facility, ResultSet>(cqlOperations.queryAsynchronously("SELECT * FROM facilities WHERE facility_id='" + facilityId + "';")) {
            @Override
            protected Facility adapt(ResultSet resultSet) throws ExecutionException {
                Row result = resultSet.one();
                if (null != result) {
                    Facility facility = new Facility();
                    facility.setFacilityId(result.getString("facility_id"));
                    facility.setFacilityName(result.getString("facility_name"));
                    facility.setCatchments(result.getString("catchments"));
                    return facility;
                } else {
                    return null;
                }
            }
        };
    }

    public void save(Facility facility) {
        cqlOperations.execute(toCQL(facility));
    }

    private String toCQL(Facility facility) {
        String query = query(asList(facility.getFacilityId(),
                facility.getFacilityName(), facility.getCatchmentsInCommaSeparatedString()));
        return "INSERT into facilities (facility_id, facility_name,catchments) values  (" + query + ")";
    }

    private String query(List<String> values) {
        return join(map(values, new CollectionUtils.Fn<String, String>() {
            public String call(String input) {
                return "'" + input + "'";
            }
        }), ",");
    }

}
