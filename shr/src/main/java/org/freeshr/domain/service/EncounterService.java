package org.freeshr.domain.service;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirValidator;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class EncounterService {

    private EncounterRepository encounterRepository;
    private PatientRegistry patientRegistry;
    private FhirValidator fhirValidator;
    private FacilityRegistry facilityRegistry;


    @Autowired
    public EncounterService(EncounterRepository encounterRepository, PatientRegistry patientRegistry, FhirValidator fhirValidator, FacilityRegistry facilityRegistry) {
        this.encounterRepository = encounterRepository;
        this.patientRegistry = patientRegistry;
        this.fhirValidator = fhirValidator;
        this.facilityRegistry = facilityRegistry;
    }

    public ListenableFuture<EncounterResponse> ensureCreated(final EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        ListenableFuture<EncounterResponse> validationResult = validate(encounterBundle);
        if (null == validationResult) {
            return new ListenableFutureAdapter<EncounterResponse, Patient>(patientRegistry.ensurePresent(encounterBundle.getHealthId())) {
                @Override
                protected EncounterResponse adapt(Patient patient) throws ExecutionException {
                    EncounterResponse response = new EncounterResponse();
                    if (patient != null) {
                        encounterBundle.setEncounterId(UUID.randomUUID().toString());
                        try {
                            encounterRepository.save(encounterBundle, patient);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            System.out.println(e);
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

    private Map<Integer, String> AddressHierarchy = new HashMap<Integer, String>() {{
        put(2, "division_id");
        put(4, "district_id");
        put(6, "upazilla_id");
        put(8, "city_corporation_id");
        put(10, "ward_id");
    }};


    public ListenableFuture<List<EncounterBundle>> findAllEncountersByCatchments(String facilityId) {
        return new ListenableFutureAdapter<List<EncounterBundle>, Facility>(facilityRegistry.ensurePresent(facilityId)) {

            @Override
            protected List<EncounterBundle> adapt(Facility facility) throws ExecutionException {
                try {
                    return new ArrayList<>(findAllEncountersByCatchments(facility.getCatchments()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private Set<EncounterBundle> findAllEncountersByCatchments(List<String> catchments) throws ExecutionException, InterruptedException {
        final Set<EncounterBundle> bundles = new HashSet<>();
        for (String catchment : catchments) {
            int length = catchment.length();

            ListenableFuture<List<EncounterBundle>> allEncountersByCatchment = encounterRepository.findAllEncountersByCatchment(catchment, AddressHierarchy.get(length));
            bundles.addAll(allEncountersByCatchment.get());

        }
        return bundles;
    }


}
