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
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.FuncN;

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

    public Observable<EncounterResponse> ensureCreated(final EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        EncounterValidationResponse validationResult = validate(encounterBundle);
        if (null == validationResult) {
            Observable<Patient> patientObservable = patientService.ensurePresent(encounterBundle.getHealthId());
            return patientObservable.flatMap(success(encounterBundle), error(), complete());

        } else {
            return Observable.just(new EncounterResponse().setValidationFailure(validationResult));
        }
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

    private Func1<Patient, Observable<EncounterResponse>> success(final EncounterBundle encounterBundle) {
        return new Func1<Patient, Observable<EncounterResponse>>() {
            @Override
            public Observable<EncounterResponse> call(Patient patient) {
                final EncounterResponse response = new EncounterResponse();
                if (patient != null) {
                    encounterBundle.setEncounterId(UUID.randomUUID().toString());
                    Observable<Boolean> save = encounterRepository.save(encounterBundle, patient);
                    return save.map(new Func1<Boolean, EncounterResponse>() {
                        @Override
                        public EncounterResponse call(Boolean aBoolean) {
                            if (aBoolean)
                                response.setEncounterId(encounterBundle.getEncounterId());
                            return response;
                        }
                    });
                } else {
                    return Observable.just(response.preconditionFailure("healthId", "invalid", "Patient not available in patient registry"));
                }
            }
        };
    }


    /**
     * @param healthId
     * @param sinceDate
     * @param limit
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Observable<List<EncounterBundle>> findEncountersForPatient(String healthId, Date sinceDate, int limit) throws ExecutionException, InterruptedException {
        return encounterRepository.findEncountersForPatient(healthId, sinceDate, limit);
    }

    private EncounterValidationResponse validate(EncounterBundle encounterBundle) {
        final EncounterValidationResponse encounterValidationResponse = encounterValidator.validate(encounterBundle);
        return encounterValidationResponse.isSuccessful() ? null : encounterValidationResponse;
    }

    /**
     * @param facilityId
     * @param date
     * @return
     * @deprecated do not use this. can not guarantee order of encounters across all catchments.
     */
    public Observable<List<EncounterBundle>> findAllEncountersByFacilityCatchments(String facilityId, final String date) {
        Observable<Facility> facilityObservable = facilityService.ensurePresent(facilityId);

        return facilityObservable.flatMap(new Func1<Facility, Observable<List<EncounterBundle>>>() {
            @Override
            public Observable<List<EncounterBundle>> call(Facility facility) {
                if(facility == null) return Observable.<List<EncounterBundle>>just(new ArrayList<EncounterBundle>());
                return findEncountersForCatchments(facility.getCatchments(), date);
            }
        }, new Func1<Throwable, Observable<List<EncounterBundle>>>() {
            @Override
            public Observable<List<EncounterBundle>> call(Throwable throwable) {
                logger.error(throwable.getMessage());
                return Observable.error(throwable);
            }

        }, new Func0<Observable<List<EncounterBundle>>>() {
            @Override
            public Observable<List<EncounterBundle>> call() {
                return null;
            }
        });
    }

    /**
     * @param facilityId
     * @param catchment
     * @param sinceDate
     * @return list of encounters. limited to 20
     */
    public Observable<List<EncounterBundle>> findEncountersForFacilityCatchment(String facilityId, final String catchment,
                                                                                final Date sinceDate, final int limit) {
        Observable<Facility> facility = facilityService.ensurePresent(facilityId);

        return facility.flatMap(new Func1<Facility, Observable<List<EncounterBundle>>>() {
            @Override
            public Observable<List<EncounterBundle>> call(Facility facility) {
                if ((null == facility)
                        || StringUtils.isBlank(catchment)
                        || !(facility.has(catchment)))
                    return Observable.<List<EncounterBundle>>just(new ArrayList<EncounterBundle>());
                return encounterRepository.findEncountersForCatchment(new Catchment(catchment), sinceDate, limit);
            }
        });
    }

    private Observable<List<EncounterBundle>> findEncountersForCatchments(final List<String> catchments, String sinceDate) {
        Date updateSince = DateUtil.parseDate(sinceDate);
        List<Observable<List<EncounterBundle>>> observablesForCatchment = new ArrayList<>();
        for (String catchment : catchments) {
            Catchment facilityCatchment = new Catchment(catchment);
            observablesForCatchment.add(encounterRepository.findEncountersForCatchment(facilityCatchment, updateSince, getEncounterFetchLimit()));
        }

        return Observable.zip(observablesForCatchment, new FuncN<List<EncounterBundle>>() {
            @Override
            public List<EncounterBundle> call(Object... args) {
                Set<EncounterBundle> uniqueEncounterBundles = new HashSet<>();
                for (Object encounterBundles : args) {
                    uniqueEncounterBundles.addAll((List<EncounterBundle>) encounterBundles);
                }
                return new ArrayList<>(uniqueEncounterBundles);
            }
        });
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
}
