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

    private final PatientRepository allPatients;
    private final MasterClientIndexWrapper masterClientIndexWrapper;

    @Autowired
    public PatientRegistry(PatientRepository allPatients, MasterClientIndexWrapper masterClientIndexWrapper) {
        this.allPatients = allPatients;
        this.masterClientIndexWrapper = masterClientIndexWrapper;
    }

    public ListenableFuture<Boolean> ensurePresent(final String healthId) {
        return new ListenableFutureAdapter<Boolean, Patient>(allPatients.find(healthId)) {
            @Override
            protected Boolean adapt(Patient result) throws ExecutionException {
                if (null == result) {
                    try {
                        return new ListenableFutureAdapter<Boolean, Patient>(masterClientIndexWrapper.getPatient(healthId)) {
                            @Override
                            protected Boolean adapt(Patient result) throws ExecutionException {
                                allPatients.save(result);
                                return null != result;
                            }
                        }.get();
                    } catch (Exception e) {
                        logger.warn(e);
                        return Boolean.FALSE;
                    }
                } else {
                    return Boolean.TRUE;
                }
            }
        };
    }
}
