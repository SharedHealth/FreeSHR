package org.freeshr.shr.encounter.service;

import org.freeshr.shr.encounter.model.Encounter;
import org.freeshr.shr.encounter.repository.AllEncounters;
import org.freeshr.shr.patient.service.PatientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.net.URI;
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

    public void ensureCreated(final Encounter encounter) throws ExecutionException, InterruptedException {
        patientRegistry.isValid(encounter.getHealthId(), new ListenableFutureCallback<URI>() {
            @Override
            public void onSuccess(URI result) {
                if (result != null) {
                    allEncounters.save(encounter);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }
}
