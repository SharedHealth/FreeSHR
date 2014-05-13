package org.freeshr.shr.encounter.service;

import org.freeshr.shr.encounter.model.Encounter;
import org.freeshr.shr.encounter.repository.AllEncounters;
import org.freeshr.shr.patient.service.PatientRegistry;
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
