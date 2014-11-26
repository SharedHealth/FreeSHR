package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MasterClientIndexClient;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.concurrent.ExecutionException;

@Service
public class PatientService {

    private Logger logger = Logger.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final MasterClientIndexClient masterClientIndexClient;

    @Autowired
    public PatientService(PatientRepository patientRepository, MasterClientIndexClient masterClientIndexClient) {
        this.patientRepository = patientRepository;
        this.masterClientIndexClient = masterClientIndexClient;
    }

    public Observable<Patient> ensurePresent(final String healthId) throws ExecutionException, InterruptedException {
        Observable<Patient> patient = patientRepository.find(healthId);
        return patient.flatMap(new Func1<Patient, Observable<Patient>>() {
            @Override
            public Observable<Patient> call(Patient patient) {
                if (null != patient) return Observable.just(patient);
                return findRemote(healthId);
            }
        });
    }

    private Observable<Patient> findRemote(String healthId) {
        Observable<Patient> remotePatient = masterClientIndexClient.getPatient(healthId);
        savePatient(remotePatient);
        return remotePatient.onErrorReturn(new Func1<Throwable, Patient>() {
            @Override
            public Patient call(Throwable throwable) {
                logger.error("Patient not found at MCI");
                return null;
            }
        });
    }

    private void savePatient(Observable<Patient> remotePatient) {
        remotePatient.subscribeOn(Schedulers.io());
        remotePatient.subscribe(new Action1<Patient>() {
            @Override
            public void call(Patient patient) {
                patientRepository.save(patient);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.error(throwable.getMessage());
            }
        });
    }
}
