package org.freeshr.domain.service;

import org.apache.commons.lang.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirValidator;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class EncounterService {
    private static final int DEFAULT_FETCH_LIMIT = 20;
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

    /**
     *
     * @param facilityId
     * @param date
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * @deprecated do not use this. can not gaurantee order of encounters across all catchments.
     */
    public List<EncounterBundle> findAllEncountersByFacilityCatchments(String facilityId, final String date) throws ExecutionException, InterruptedException {
        List<EncounterBundle> encounterBundles = new ArrayList<>();
        Facility facility = facilityService.ensurePresent(facilityId);
        if (null == facility) return encounterBundles;
        return findEncountersForCatchments(facility.getCatchments(), date);
    }

    /**
     *
     * @param facilityId
     * @param catchment
     * @param sinceDate
     * @return list of encounters. limited to 20
     * @throws ExecutionException
     * @throws InterruptedException
     * @deprecated Don't use this method. Can't gaurantee order over paged requests.
     */
    public List<EncounterBundle> findEncountersForFacilityCatchment(String facilityId, String catchment, final String sinceDate) throws ExecutionException, InterruptedException {
        Date updateSince = parseDate(sinceDate);

        List<EncounterBundle> encounterBundles = new ArrayList<>();
        Facility facility = facilityService.ensurePresent(facilityId);

        if (null == facility) return encounterBundles;
        if (StringUtils.isBlank(catchment)) return encounterBundles;
        if (!facility.has(catchment)) return encounterBundles; //TODO rule check if we throw error!
        return encounterRepository.findEncountersForCatchment(new Catchment(catchment), updateSince, DEFAULT_FETCH_LIMIT);
    }

    private Date parseDate(final String sinceDate) {
        try {
            return DateUtil.parseDate(sinceDate, DateUtil.DATE_FORMATS);
        } catch (ParseException e) {
            throw new RuntimeException("invalid date:" + sinceDate);
        }
    }

    private List<EncounterBundle> findEncountersForCatchments(final List<String> catchments, String sinceDate) throws ExecutionException, InterruptedException {
        Set<EncounterBundle> encounters = new HashSet<>();
        Date updateSince = parseDate(sinceDate);
        for (String catchment : catchments) {
            Catchment facilityCatchment = new Catchment(catchment);
            encounters.addAll(encounterRepository.findEncountersForCatchment(facilityCatchment, updateSince, DEFAULT_FETCH_LIMIT));
        }
        return new ArrayList<>(encounters);
    }


}
