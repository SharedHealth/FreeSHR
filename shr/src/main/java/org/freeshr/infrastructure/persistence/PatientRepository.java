package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.freeshr.utils.concurrent.SimpleListenableFuture;
import org.freeshr.domain.model.patient.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

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
                    patient.setHealthId(result.getString("health_id"));
                    return patient;
                } else {
                    return null;
                }
            }
        };
    }

    public void save(Patient patient) {
        cqlOperations.executeAsynchronously("INSERT into patient (health_id) values  ('" + patient.getHealthId() + "')");
    }
}
