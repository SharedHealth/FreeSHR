package org.freeshr.domain.service;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.events.EncounterEvent;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.EncounterValidator;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.lang.Boolean;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.Confidentiality.getConfidentiality;

@Service
public class PatientEncounterService {
    private EncounterRepository encounterRepository;
    private PatientService patientService;
    private EncounterValidator encounterValidator;

    private static final Logger logger = LoggerFactory.getLogger(PatientEncounterService.class);

    @Autowired
    public PatientEncounterService(EncounterRepository encounterRepository, PatientService patientService,
                                   EncounterValidator encounterValidator) {
        this.encounterRepository = encounterRepository;
        this.patientService = patientService;
        this.encounterValidator = encounterValidator;
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

    public Observable<EncounterBundle> findEncounter(final String healthId, String encounterId) {
        return encounterRepository.findEncounterById(encounterId).filter(new Func1<EncounterBundle, Boolean>() {
            @Override
            public Boolean call(EncounterBundle encounterBundle) {
                return encounterBundle.getHealthId().equals(healthId);
            }
        });
    }

    public Observable<List<EncounterBundle>> findEncountersForPatient(String healthId, Date sinceDate,
                                                                      int limit) throws ExecutionException,
            InterruptedException {
        return encounterRepository.findEncountersForPatient(healthId, sinceDate, limit);
    }

    public Observable<List<EncounterEvent>> findEncounterFeedForPatient(String healthId, Date sinceDate,
                                                                        int limit) throws ExecutionException,
            InterruptedException {
        return encounterRepository.findEncounterFeedForPatient(healthId, sinceDate, limit);
    }

    public Observable<EncounterResponse> ensureUpdated(final EncounterBundle encounterBundle, final UserInfo userInfo) throws ExecutionException, InterruptedException {
        final EncounterValidationResponse validationResult = validate(encounterBundle);
        if (validationResult.isSuccessful()) {
            Observable<Patient> patientObservable = patientService.ensurePresent(encounterBundle.getHealthId(),
                    userInfo);
            Func1<? super Patient, ? extends Observable<? extends EncounterResponse>> patientFetchSuccessCallBack = new Func1<Patient, Observable<? extends EncounterResponse>>() {
                @Override
                public Observable<? extends EncounterResponse> call(Patient patient) {
                    if (patient != null) {
                        Observable<EncounterBundle> encounterFetchObservable = findEncounter(encounterBundle.getHealthId(), encounterBundle.getEncounterId()).firstOrDefault(null);
                        return encounterFetchObservable.flatMap(encounterFetchSuccessCallbackForUpdate(encounterBundle, patient, validationResult.getFeed(), userInfo), error(), complete());
                    } else {
                        return Observable.just(new EncounterResponse().preconditionFailure("healthId", "invalid",
                                "Patient not available in patient registry"));
                    }
                }
            };
            return patientObservable.flatMap(patientFetchSuccessCallBack, error(), complete());

        } else {
            return Observable.just(new EncounterResponse().setValidationFailure(validationResult));
        }
    }

    private Func1<? super EncounterBundle, ? extends Observable<? extends EncounterResponse>> encounterFetchSuccessCallbackForUpdate(
            final EncounterBundle encounterBundle, final Patient patient, final AtomFeed feed, final UserInfo userInfo) {
        return new Func1<EncounterBundle, Observable<EncounterResponse>>() {
            @Override
            public Observable<EncounterResponse> call(EncounterBundle existingEncounterBundle) {
                if(existingEncounterBundle != null){
                    return updateEncounter(existingEncounterBundle, userInfo, encounterBundle, feed, patient);
                } else{
                    return Observable.just(new EncounterResponse().preconditionFailure("encounterId", "invalid",
                            String.format("Encounter (%s) not available.", encounterBundle.getEncounterId())));
                }
            }
        };
    }

    private Observable<EncounterResponse> updateEncounter(EncounterBundle existingEncounterBundle, UserInfo userInfo, final EncounterBundle encounterBundle, AtomFeed feed, Patient patient) {
        Requester updatedBy = new Requester(userInfo.getProperties().getFacilityId(), userInfo.getProperties().getProviderId());
        if(isEncounterEditAllowed(existingEncounterBundle, updatedBy)){
            final EncounterResponse response = new EncounterResponse();
            encounterBundle.setEncounterConfidentiality(getEncounterConfidentiality(feed));
            encounterBundle.setUpdatedAt(new Date());
            encounterBundle.setUpdatedBy(updatedBy);
            encounterBundle.setContentVersion(existingEncounterBundle.getContentVersion() +1);
            encounterBundle.setReceivedAt(existingEncounterBundle.getReceivedAt());

            Observable<Boolean> update = encounterRepository.updateEncounter(encounterBundle, existingEncounterBundle, patient);

            return update.flatMap(new Func1<Boolean, Observable<EncounterResponse>>() {
                @Override
                public Observable<EncounterResponse> call(Boolean aBoolean) {
                    if (aBoolean)
                        response.setEncounterId(encounterBundle.getEncounterId());
                    return Observable.just(response);
                }
            }, error(), complete());
        }else {
            return Observable.just(new EncounterResponse().forbidden("updatedBy", "not authorized",
                    String.format("Access is denied to requester (%s) to edit the encounter", updatedBy)));
        }
    }

    private boolean isEncounterEditAllowed(EncounterBundle existingEncounterBundle, Requester updatedBy) {
        return existingEncounterBundle.getCreatedBy().equals(updatedBy);
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
                logger.debug(throwable.getMessage());
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
        Date currentTimestamp = new Date();
        encounterBundle.setReceivedAt(currentTimestamp);
        encounterBundle.setUpdatedAt(currentTimestamp);

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
