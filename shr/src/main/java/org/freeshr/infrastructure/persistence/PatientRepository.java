package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
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
public class PatientRepository {

    private CqlOperations cqlOperations;

    @Autowired
    public PatientRepository(@Qualifier("SHRCassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public ListenableFuture<Patient> find(String healthId) {
        return new SimpleListenableFuture<Patient, ResultSet>(cqlOperations.queryAsynchronously("SELECT * FROM patient WHERE health_id='" + healthId + "';")) {
            @Override
            protected Patient adapt(ResultSet resultSet) throws ExecutionException {
                Row result = resultSet.one();
                if (null != result) {
                    Patient patient = new Patient();
                    Address address = new Address();
                    patient.setHealthId(result.getString("health_id"));
                    patient.setGender(result.getString("gender"));
                    address.setLine(result.getString("address_line"));
                    address.setDistrict(result.getString("district_id"));
                    address.setWard(result.getString("ward_id"));
                    address.setCityCorporation(result.getString("city_corporation_id"));
                    address.setUpazilla(result.getString("upazilla_id"));
                    address.setDivision(result.getString("division_id"));
                    patient.setAddress(address);
                    return patient;
                } else {
                    return null;
                }
            }
        };
    }

    public void save(Patient patient) {
        //TODO:use query builder
        cqlOperations.executeAsynchronously(toCQL(patient));
    }

    public void saveSynchronously(Patient patient){
        cqlOperations.execute(toCQL(patient));
    }

    private String toCQL(Patient patient) {
        Address address = patient.getAddress();
        String query = query(asList(patient.getHealthId(),
                patient.getGender(), address.getLine(), address.getDistrict(),
                address.getDivision(), address.getWard(), address.getUpazilla(), address.getCityCorporation()));
        return "INSERT into patient (health_id, gender, address_line, district_id, division_id, ward_id, upazilla_id, city_corporation_id) values  (" + query + ")";
    }



    private String query(List<String> values) {
        return join(map(values, new CollectionUtils.Fn<String, String>() {
            public String call(String input) {
                return "'" + input + "'";
            }
        }), ",");
    }
}
