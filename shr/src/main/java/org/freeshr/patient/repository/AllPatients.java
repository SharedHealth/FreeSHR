package org.freeshr.patient.repository;

import org.freeshr.patient.model.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllPatients {

    private List<Patient> allPatients = new ArrayList<Patient>();

    public Patient find(String healthId) {
        for (Patient patient : allPatients) {
            if (healthId.equals(patient.getProfile().getHID())) {
                return patient;
            }
        }
        return null;
    }
}
