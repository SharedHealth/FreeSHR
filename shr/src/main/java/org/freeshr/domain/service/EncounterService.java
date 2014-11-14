package org.freeshr.domain.service;

import org.apache.commons.lang.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.EncounterValidator;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class EncounterService {
    private static final int DEFAULT_FETCH_LIMIT = 20;
    private static final String ENCOUNTER_FETCH_LIMIT_LOOKUP_KEY = "ENCOUNTER_FETCH_LIMIT";
    private EncounterRepository encounterRepository;
    private PatientService patientService;
    private EncounterValidator encounterValidator;
    private FacilityService facilityService;

    private static final Logger logger = LoggerFactory.getLogger(EncounterService.class);

    @Autowired
    public EncounterService(EncounterRepository encounterRepository, PatientService patientService, EncounterValidator encounterValidator, FacilityService facilityService) {
        this.encounterRepository = encounterRepository;
        this.patientService = patientService;
        this.encounterValidator = encounterValidator;
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

    /**
     *
     * @param healthId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * @deprecated do not use this query
     */
    public List<EncounterBundle> findAll(String healthId) throws ExecutionException, InterruptedException {
        //TODO refactor
        return encounterRepository.findAll(healthId);
    }

    private EncounterValidationResponse validate(EncounterBundle encounterBundle) {
        final EncounterValidationResponse encounterValidationResponse = encounterValidator.validate(encounterBundle);
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
     * @param defaultFetchLimit
     * @return list of encounters. limited to 20
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<EncounterBundle> findEncountersForFacilityCatchment(String facilityId, String catchment, final Date sinceDate, int limit) throws ExecutionException, InterruptedException {
        List<EncounterBundle> encounterBundles = new ArrayList<>();
        Facility facility = facilityService.ensurePresent(facilityId);

        if (null == facility) return encounterBundles;
        if (StringUtils.isBlank(catchment)) return encounterBundles;
        if (!facility.has(catchment)) return encounterBundles; //TODO rule check if we throw error!
        return encounterRepository.findEncountersForCatchment(new Catchment(catchment), sinceDate, limit);
    }

    private List<EncounterBundle> findEncountersForCatchments(final List<String> catchments, String sinceDate) throws ExecutionException, InterruptedException {
        Set<EncounterBundle> encounters = new HashSet<>();
        Date updateSince = DateUtil.parseDate(sinceDate);
        for (String catchment : catchments) {
            Catchment facilityCatchment = new Catchment(catchment);
            encounters.addAll(encounterRepository.findEncountersForCatchment(facilityCatchment, updateSince, getEncounterFetchLimit()));
        }
        return new ArrayList<>(encounters);
    }

    public static int getEncounterFetchLimit() {
        Map<String, String> env = System.getenv();
        String encounterFetchLimit = env.get(ENCOUNTER_FETCH_LIMIT_LOOKUP_KEY);
        int fetchLimit = DEFAULT_FETCH_LIMIT;
        if (!StringUtils.isBlank(encounterFetchLimit)) {
            try {
                fetchLimit = Integer.valueOf(encounterFetchLimit);
            } catch (NumberFormatException nfe) {
                //Do nothing
            }
        }
        return fetchLimit;
    }



}
