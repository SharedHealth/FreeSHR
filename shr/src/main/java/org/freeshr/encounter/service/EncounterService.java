package org.freeshr.encounter.service;

import org.freeshr.encounter.model.Encounter;
import org.freeshr.encounter.repository.AllEncounters;
import org.freeshr.patient.service.PatientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncounterService {

    private AllEncounters allEncounters;
    private PatientRegistry patientRegistry;

    @Autowired
    public EncounterService(AllEncounters allEncounters, PatientRegistry patientRegistry) {
        this.allEncounters = allEncounters;
        this.patientRegistry = patientRegistry;
    }

    public void ensureCreated(Encounter encounter) {
        Boolean valid = patientRegistry.isValid(encounter.getHealthId());
        if (valid) {
            allEncounters.save(encounter);
        }
    }
}
