package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MasterClientIndexClient;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

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
        return remotePatient.flatMap(new Func1<Patient, Observable<Patient>>() {
            @Override
            public Observable<Patient> call(Patient patient) {
                if (null != patient) {
                    patientRepository.save(patient);
                }
                return Observable.just(patient);
            }
        }, new Func1<Throwable, Observable<Patient>>() {
            @Override
            public Observable<Patient> call(Throwable throwable) {
                logger.error(throwable);
                return Observable.just(null);
            }
        }, new Func0<Observable<Patient>>() {
            @Override
            public Observable<Patient> call() {
                return null;
            }
        });
    }
}
