package org.freeshr.domain.service;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirValidator;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class EncounterService {

    private EncounterRepository encounterRepository;
    private PatientService patientService;
    private FhirValidator fhirValidator;
    private FacilityService facilityService;

    private static final Logger logger = LoggerFactory.getLogger(EncounterService.class);

    @Autowired
    public EncounterService(EncounterRepository encounterRepository, PatientService patientService, FhirValidator fhirValidator, FacilityService facilityService) {
        this.encounterRepository = encounterRepository;
        this.patientService = patientService;
        this.fhirValidator = fhirValidator;
        this.facilityService = facilityService;
    }

    public EncounterResponse ensureCreated(final EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        EncounterValidationResponse validationResult = validate(encounterBundle);
        if (null == validationResult) {
            Patient patient = patientService.ensurePresent(encounterBundle.getHealthId());
            EncounterResponse response = new EncounterResponse();
            if (patient != null) {
                encounterBundle.setEncounterId(UUID.randomUUID().toString());
                try {
                    encounterRepository.save(encounterBundle, patient);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
                response.setEncounterId(encounterBundle.getEncounterId());
                return response;
            } else {
                return response.preconditionFailure("healthId", "invalid", "Patient not available in patient registry");
            }
        } else {
            return new EncounterResponse().setValidationFailure(validationResult);
        }
    }

    public List<EncounterBundle> findAll(String healthId) throws ExecutionException, InterruptedException {
        return encounterRepository.findAll(healthId);
    }

    private EncounterValidationResponse validate(EncounterBundle encounterBundle) {
        final EncounterValidationResponse encounterValidationResponse = fhirValidator.validate(encounterBundle.getEncounterContent().toString());
        return encounterValidationResponse.isSuccessful() ? null : encounterValidationResponse;
    }

    public List<EncounterBundle> findEncountersByCatchments(String facilityId, final String date) throws ExecutionException, InterruptedException {
        List<EncounterBundle> encounterBundles = new ArrayList<>();
        Facility facility = facilityService.ensurePresent(facilityId);
        if (null == facility) return encounterBundles;
        return findEncountersByFacility(facility, date);
    }

    private List<EncounterBundle> findEncountersByFacility(Facility facility, String date) throws ExecutionException, InterruptedException {
        Set<EncounterBundle> encounters = new HashSet<>();
        for (String catchment : facility.getCatchments()) {
            FacilityCatchment facilityCatchment = new FacilityCatchment(catchment);
            encounters.addAll(encounterRepository.findAllEncountersByCatchment(facilityCatchment.getCatchment(),
                    facilityCatchment.getCatchmentType(), date));
        }
        return new ArrayList<>(encounters);
    }

    private Map<Integer, String> AddressHierarchy = new HashMap<Integer, String>() {{
        put(2, "division_id");
        put(4, "district_id");
        put(6, "upazilla_id");
        put(8, "city_corporation_id");
        put(10, "ward_id");
    }};

    private class FacilityCatchment {
        private final String catchment;

        public FacilityCatchment(String catchment) {
            this.catchment = catchment;
        }

        public String getCatchment() {
            return this.catchment;
        }

        public String getCatchmentType() {
            int length = this.catchment.length();
            return AddressHierarchy.get(length);
        }


    }


}
