package org.freeshr.domain.service;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
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

    public ListenableFuture<EncounterResponse> ensureCreated(final EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        ListenableFuture<EncounterResponse> validationResult = validate(encounterBundle);
        if (null == validationResult) {
            return new ListenableFutureAdapter<EncounterResponse, Boolean>(patientRegistry.ensurePresent(encounterBundle.getHealthId())) {
                @Override
                protected EncounterResponse adapt(Boolean result) throws ExecutionException {
                    EncounterResponse response = new EncounterResponse();
                    if (result) {
                        encounterBundle.setEncounterId(UUID.randomUUID().toString());
                        try {
                            encounterRepository.save(encounterBundle);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        response.setEncounterId(encounterBundle.getEncounterId());
                        return response;
                    } else {
                        return response.preconditionFailure("healthId", "invalid", "Patient not available in patient registry");
                    }
                }
            };
        } else {
            return validationResult;
        }
    }

    private void extractPatientLocation(EncounterBundle encounterBundle) {

    }

    public ListenableFuture<List<EncounterBundle>> findAll(String healthId) {
        return new ListenableFutureAdapter<List<EncounterBundle>, List<EncounterBundle>>(encounterRepository.findAll(healthId)) {
            @Override
            protected List<EncounterBundle> adapt(List<EncounterBundle> bundles) throws ExecutionException {
                return bundles;
            }
        };
    }

    private ListenableFuture<EncounterResponse> validate(EncounterBundle encounterBundle) {
        final EncounterValidationResponse encounterValidationResponse = fhirValidator.validate(encounterBundle.getEncounterContent().toString());
        if (!encounterValidationResponse.isSuccessful()) {
            return new PreResolvedListenableFuture<>(new EncounterResponse().setValidationFailure(encounterValidationResponse));
        } else {
            return null;
        }
    }
}
