package org.freeshr.domain.service;

import org.apache.log4j.Logger;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MasterClientIndexClient;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Patient ensurePresent(final String healthId) throws ExecutionException, InterruptedException {
        try {
            Patient patient = patientRepository.find(healthId);
            if (null != patient) return patient;
            patient = masterClientIndexClient.getPatient(healthId).get();
            if (null == patient) return null;
            patientRepository.save(patient);
            return patient;
        }
        catch (Exception e){
            logger.warn(e);
            return null;
        }
    }
}
