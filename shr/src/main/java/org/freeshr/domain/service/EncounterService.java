package org.freeshr.domain.service;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.FhirValidator;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class EncounterService {

    private EncounterRepository encounterRepository;
    private PatientRegistry patientRegistry;
    private FhirValidator fhirValidator;

    @Autowired
    public EncounterService(EncounterRepository encounterRepository, PatientRegistry patientRegistry, FhirValidator fhirValidator) {
        this.encounterRepository = encounterRepository;
        this.patientRegistry = patientRegistry;
        this.fhirValidator = fhirValidator;
    }

    public ListenableFuture<String> ensureCreated(final EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        if (fhirValidator.validate(encounterBundle.getEncounterContent().toString())) {
            return new ListenableFutureAdapter<String, Boolean>(patientRegistry.ensurePresent(encounterBundle.getHealthId())) {
                @Override
                protected String adapt(Boolean result) throws ExecutionException {
                    if (result) {
                        encounterBundle.setEncounterId(UUID.randomUUID().toString());
                        encounterRepository.save(encounterBundle);
                        return encounterBundle.getEncounterId();
                    }
                    return null;
                }
            };
        } else {
            return new PreResolvedListenableFuture<>(null);
        }
    }

    public ListenableFuture<List<EncounterBundle>> findAll(String healthId) {
        return new ListenableFutureAdapter<List<EncounterBundle>, List<EncounterBundle>>(encounterRepository.findAll(healthId)) {
            @Override
            protected List<EncounterBundle> adapt(List<EncounterBundle> bundles) throws ExecutionException {
                return bundles;
            }
        };
    }
}
