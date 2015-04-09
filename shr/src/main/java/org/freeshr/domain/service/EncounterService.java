package org.freeshr.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.EncounterValidator;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.Confidentiality.getConfidentiality;
import static org.freeshr.utils.DateUtil.getCurrentTimeInISOString;

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
    public EncounterService(EncounterRepository encounterRepository, PatientService patientService,
                            EncounterValidator encounterValidator, FacilityService facilityService) {
        this.encounterRepository = encounterRepository;
        this.patientService = patientService;
        this.encounterValidator = encounterValidator;
        this.facilityService = facilityService;
    }

    public Observable<EncounterResponse> ensureCreated(final EncounterBundle encounterBundle, UserInfo userInfo)
            throws
            ExecutionException, InterruptedException {
        EncounterValidationResponse validationResult = validate(encounterBundle);
        if (validationResult.isSuccessful()) {
            Observable<Patient> patientObservable = patientService.ensurePresent(encounterBundle.getHealthId(),
                    userInfo);
            return patientObservable.flatMap(success(encounterBundle, validationResult.getFeed(), userInfo), error(), complete());

        } else {
            return Observable.just(new EncounterResponse().setValidationFailure(validationResult));
        }
    }

    /**
     * @param healthId
     * @param sinceDate
     * @param limit
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Observable<List<EncounterBundle>> findEncountersForPatient(String healthId, Date sinceDate,
                                                                      int limit) throws ExecutionException,
            InterruptedException {
        return encounterRepository.findEncountersForPatient(healthId, sinceDate, limit);
    }

    /**
     * @param facilityId
     * @param catchment
     * @param sinceDate
     * @return list of encounters. limited to 20
     */
    public Observable<List<EncounterBundle>> findEncountersForFacilityCatchment(final String catchment,
                                                                                final Date sinceDate, final int limit) {
        return encounterRepository.findEncountersForCatchment(new Catchment(catchment), sinceDate, limit);
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

    public Observable<EncounterBundle> findEncounter(final String healthId, String encounterId) {
        return encounterRepository.findEncounterById(encounterId).filter(new Func1<EncounterBundle, Boolean>() {
            @Override
            public Boolean call(EncounterBundle encounterBundle) {
                return encounterBundle.getHealthId().equals(healthId);
            }
        });
    }

    private Func0<Observable<EncounterResponse>> complete() {
        return new Func0<Observable<EncounterResponse>>() {
            @Override
            public Observable<EncounterResponse> call() {
                return null;
            }
        };
    }

    private Func1<Throwable, Observable<EncounterResponse>> error() {
        return new Func1<Throwable, Observable<EncounterResponse>>() {
            @Override
            public Observable<EncounterResponse> call(Throwable throwable) {
                logger.error(throwable.getMessage());
                return Observable.error(throwable);
            }
        };
    }

    private Func1<Patient, Observable<EncounterResponse>> success(final EncounterBundle encounterBundle, final AtomFeed feed, final UserInfo userInfo) {
        return new Func1<Patient, Observable<EncounterResponse>>() {
            @Override
            public Observable<EncounterResponse> call(Patient patient) {
                final EncounterResponse response = new EncounterResponse();
                if (patient != null) {
                    populateEncounterBundleFields(patient, encounterBundle, feed, userInfo);

                    Observable<Boolean> save = encounterRepository.save(encounterBundle, patient);

                    return save.flatMap(new Func1<Boolean, Observable<EncounterResponse>>() {
                        @Override
                        public Observable<EncounterResponse> call(Boolean aBoolean) {
                            if (aBoolean)
                                response.setEncounterId(encounterBundle.getEncounterId());
                            return Observable.just(response);
                        }
                    }, error(), complete());

                } else {
                    return Observable.just(response.preconditionFailure("healthId", "invalid",
                            "Patient not available in patient registry"));
                }
            }
        };
    }

    private void populateEncounterBundleFields(Patient patient, EncounterBundle encounterBundle, AtomFeed feed, UserInfo userInfo) {
        encounterBundle.setEncounterId(UUID.randomUUID().toString());
        encounterBundle.setPatientConfidentiality(patient.getConfidentiality());
        encounterBundle.setEncounterConfidentiality(getEncounterConfidentiality(feed));
        String currentTimestamp = getCurrentTimeInISOString();
        encounterBundle.setReceivedDate(currentTimestamp);
        encounterBundle.setUpdatedDate(currentTimestamp);

        Requester requester = new Requester(userInfo.getProperties().getFacilityId(), userInfo.getProperties().getProviderId());
        encounterBundle.setCreatedBy(requester);
        encounterBundle.setUpdatedBy(requester);
    }

    private EncounterValidationResponse validate(EncounterBundle encounterBundle) {
        EncounterValidationContext validationContext = new EncounterValidationContext(encounterBundle, new ResourceOrFeedDeserializer());
        return encounterValidator.validate(validationContext);
    }


    private Confidentiality getEncounterConfidentiality(AtomFeed feed) {
        Confidentiality encounterConfidentiality = Confidentiality.Normal;
        for (AtomEntry<? extends Resource> entry : feed.getEntryList()) {
            if (entry.getResource().getResourceType().equals(ResourceType.Composition)) {
                Composition composition = (Composition) entry.getResource();
                Coding confidentiality = composition.getConfidentiality();
                if (null == confidentiality) {
                    break;
                }
                String code = confidentiality.getCodeSimple();
                encounterConfidentiality = getConfidentiality(code);
                break;
            }
        }
        return encounterConfidentiality;
    }
}
