package org.freeshr.shr.patient.service;

import org.freeshr.shr.concurrent.PreResolvedListenableFuture;
import org.freeshr.shr.patient.repository.AllPatients;
import org.freeshr.shr.patient.wrapper.MasterClientIndexWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

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
        if (allPatients.find(healthId) != null) {
            return new PreResolvedListenableFuture<Boolean>(Boolean.TRUE);
        } else {
            return masterClientIndexWrapper.isValid(healthId);
        }
    }
}
