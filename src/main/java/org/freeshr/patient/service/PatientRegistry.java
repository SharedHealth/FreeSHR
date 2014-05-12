package org.freeshr.patient.service;

import org.freeshr.patient.repository.AllPatients;
import org.freeshr.patient.wrapper.MasterClientIndexWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientRegistry {

    private final AllPatients allPatients;
    private final MasterClientIndexWrapper masterClientIndexWrapper;

    @Autowired
    public PatientRegistry(AllPatients allPatients, MasterClientIndexWrapper masterClientIndexWrapper) {
        this.allPatients = allPatients;
        this.masterClientIndexWrapper = masterClientIndexWrapper;
    }

    public Boolean isValid(final String healthId) {
        return (allPatients.find(healthId) != null) || masterClientIndexWrapper.isValid(healthId);
    }
}
