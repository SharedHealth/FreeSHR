package org.freeshr.shr.encounter.service;

import org.freeshr.shr.encounter.model.Encounter;
import org.freeshr.shr.encounter.repository.AllEncounters;
import org.freeshr.shr.patient.service.PatientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.concurrent.ExecutionException;

@Service
public class EncounterService {

    private AllEncounters allEncounters;
    private PatientRegistry patientRegistry;

    @Autowired
    public EncounterService(AllEncounters allEncounters, PatientRegistry patientRegistry) {
        this.allEncounters = allEncounters;
        this.patientRegistry = patientRegistry;
    }

    public ListenableFuture<Boolean> ensureCreated(final Encounter encounter) throws ExecutionException, InterruptedException {
        return new ListenableFutureAdapter<Boolean, Boolean>(patientRegistry.isValid(encounter.getHealthId())) {
            @Override
            protected Boolean adapt(Boolean result) throws ExecutionException {
                if (result) {
                    allEncounters.save(encounter);
                }
                return result;
            }
        };
    }
}
