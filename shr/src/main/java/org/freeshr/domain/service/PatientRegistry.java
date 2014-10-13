package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MasterClientIndexWrapper;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.concurrent.ExecutionException;

@Service
public class PatientRegistry {

    private Logger logger = Logger.getLogger(PatientRegistry.class);

    private final PatientRepository patientRepository;
    private final MasterClientIndexWrapper masterClientIndexWrapper;

    @Autowired
    public PatientRegistry(PatientRepository patientRepository, MasterClientIndexWrapper masterClientIndexWrapper) {
        this.patientRepository = patientRepository;
        this.masterClientIndexWrapper = masterClientIndexWrapper;
    }

    public ListenableFuture<Patient> ensurePresent(final String healthId) {
        return new ListenableFutureAdapter<Patient, Patient>(patientRepository.find(healthId)) {
            @Override
            protected Patient adapt(Patient result) throws ExecutionException {
                if (null == result) {
                    try {
                        return new ListenableFutureAdapter<Patient, Patient>(masterClientIndexWrapper.getPatient(healthId)) {
                            @Override
                            protected Patient adapt(Patient result) throws ExecutionException {
                                patientRepository.save(result);
                                return result;
                            }
                        }.get();
                    } catch (Exception e) {
                        logger.warn(e);
                        return null;
                    }
                } else {
                    return result;
                }
            }
        };
    }
}
