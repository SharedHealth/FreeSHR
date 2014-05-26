package org.freeshr.shr.patient.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.shr.concurrent.SimpleListenableFuture;
import org.freeshr.shr.patient.model.Patient;
import org.freeshr.shr.patient.model.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@Component
public class AllPatients {

    private CqlOperations cqlOperations;

    @Autowired
    public AllPatients(@Qualifier("SHRCassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public ListenableFuture<Patient> find(String healthId) {
        return new SimpleListenableFuture<Patient, ResultSet>(cqlOperations.queryAsynchronously("SELECT * FROM patient WHERE health_id='" + healthId + "';")) {
            @Override
            protected Patient adapt(ResultSet resultSet) throws ExecutionException {
                Row result = resultSet.one();
                if (null != result) {
                    Profile profile = new Profile();
                    Patient patient = new Patient(profile);
                    profile.setHID(result.getString("health_id"));
                    return patient;
                } else {
                    return null;
                }
            }
        };
    }

    public void save(Patient patient) {
        cqlOperations.executeAsynchronously("INSERT into patient (healthId) values  ('" + patient.getProfile().getHID() + "')");
    }
}
