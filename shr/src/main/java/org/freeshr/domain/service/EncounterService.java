package org.freeshr.domain.service;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.UUID;
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

    public ListenableFuture<String> ensureCreated(final EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        return new ListenableFutureAdapter<String, Boolean>(patientRegistry.ensurePresent(encounterBundle.getId())) {
            @Override
            protected String adapt(Boolean result) throws ExecutionException {
                if (result) {
                    encounterBundle.setId(UUID.randomUUID().toString());
                    entcounterRepository.save(encounterBundle);
                    return encounterBundle.getId();
                }
                return null;
            }
        };
    }
}
