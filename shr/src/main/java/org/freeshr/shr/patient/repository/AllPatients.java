package org.freeshr.shr.patient.repository;

import com.datastax.driver.core.Row;
import org.freeshr.shr.patient.model.Patient;
import org.freeshr.shr.patient.model.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;

@Component
public class AllPatients {

    private CqlOperations cqlOperations;

    @Autowired
    public AllPatients(@Qualifier("SHRCassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public Patient find(String healthId) {
        Row result = cqlOperations.query("SELECT * FROM patient WHERE health_id='" + healthId + "';").one();
        if (null != result) {
            Profile profile = new Profile();
            Patient patient = new Patient(profile);
            profile.setHID(result.getString("health_id"));
            return patient;
        } else {
            return null;
        }
    }
}
