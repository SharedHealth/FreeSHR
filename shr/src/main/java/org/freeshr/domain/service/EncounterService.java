package org.freeshr.domain.service;

import org.freeshr.domain.model.encounter.Encounter;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.concurrent.ExecutionException;

@Service
public class EncounterService {

    private EncounterRepository entcounterRepository;
    private PatientRegistry patientRegistry;

    @Autowired
    public EncounterService(EncounterRepository entcounterRepository, PatientRegistry patientRegistry) {
        this.entcounterRepository = entcounterRepository;
        this.patientRegistry = patientRegistry;
    }

    public ListenableFuture<Boolean> ensureCreated(final Encounter encounter) throws ExecutionException, InterruptedException {
        return new ListenableFutureAdapter<Boolean, Boolean>(patientRegistry.ensurePresent(encounter.getHealthId())) {
            @Override
            protected Boolean adapt(Boolean result) throws ExecutionException {
                if (result) {
                    entcounterRepository.save(encounter);
                }
                return result;
            }
        };
    }
}
