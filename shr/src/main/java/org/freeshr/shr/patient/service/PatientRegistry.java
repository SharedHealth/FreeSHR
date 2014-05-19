package org.freeshr.shr.patient.service;

import org.freeshr.shr.patient.model.Patient;
import org.freeshr.shr.patient.repository.AllPatients;
import org.freeshr.shr.patient.wrapper.MasterClientIndexWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.concurrent.ExecutionException;

@Service
public class PatientRegistry {

    private final AllPatients allPatients;
    private final MasterClientIndexWrapper masterClientIndexWrapper;

    @Autowired
    public PatientRegistry(AllPatients allPatients, MasterClientIndexWrapper masterClientIndexWrapper) {
        this.allPatients = allPatients;
        this.masterClientIndexWrapper = masterClientIndexWrapper;
    }

    public ListenableFuture<Boolean> isValid(final String healthId) {
        return new ListenableFutureAdapter<Boolean, Patient>(allPatients.find(healthId)) {
            @Override
            protected Boolean adapt(Patient result) throws ExecutionException {
                if (null == result) {
                    try {
                        return masterClientIndexWrapper.isValid(healthId).get();
                    } catch (Exception e) {
                        throw new ExecutionException(e);
                    }
                } else {
                    return Boolean.FALSE;
                }
            }
        };
    }
}
