package org.freeshr.domain.service;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirValidator;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import rx.Observable;
import rx.exceptions.CompositeException;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.FuncN;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.concurrent.FutureConverter.toListenableFuture;
import static org.freeshr.utils.concurrent.FutureConverter.toObservable;

@Service
public class EncounterService {

    private EncounterRepository encounterRepository;
    private PatientRegistry patientRegistry;
    private FhirValidator fhirValidator;
    private FacilityRegistry facilityRegistry;

    private static final Logger logger = LoggerFactory.getLogger(EncounterService.class);

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

    public ListenableFuture<List<EncounterBundle>> findEncountersByCatchments(String facilityId, final String date) throws ExecutionException, InterruptedException {

        final Observable<List<EncounterBundle>> emptyPromise = toObservable(new PreResolvedListenableFuture<List<EncounterBundle>>(new ArrayList<EncounterBundle>()));

        Observable<Facility> facility = toObservable(facilityRegistry.ensurePresent(facilityId));

        Observable<List<EncounterBundle>> encounterBundles = facility.flatMap(new Func1<Facility, Observable<List<EncounterBundle>>>() {
            @Override
            public Observable<List<EncounterBundle>> call(Facility facility) {
                if (null == facility) return emptyPromise;
                return findEncountersByFacility(date, facility);
            }
        });
        return toListenableFuture(encounterBundles);
    }

    private Observable<List<EncounterBundle>> findEncountersByFacility(String date, Facility facility) {
        List<Observable<List<EncounterBundle>>> observables = new ArrayList<>();
        for (String catchment : facility.getCatchments()) {
            int length = catchment.length();
            final ListenableFuture<List<EncounterBundle>> allEncountersByCatchment = encounterRepository.findAllEncountersByCatchment(catchment, AddressHierarchy.get(length), date);
            Observable<List<EncounterBundle>> observable = toObservable(allEncountersByCatchment);
            observables.add(observable);
        }
        Observable<List<EncounterBundle>> observable = Observable.zip(observables, new FuncN<List<EncounterBundle>>() {
            @Override
            public List<EncounterBundle> call(Object... args) {
                final Set<EncounterBundle> bundles = new HashSet<>();
                for (Object arg : args) {
                    bundles.addAll((List<EncounterBundle>) arg);
                }
                return new ArrayList<EncounterBundle>(bundles);
            }
        });
        observable.doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                ArrayList<Throwable> errors = new ArrayList<>();
                errors.add(throwable);
                throw new CompositeException(errors);
            }
        });
        return observable;
    }


}
