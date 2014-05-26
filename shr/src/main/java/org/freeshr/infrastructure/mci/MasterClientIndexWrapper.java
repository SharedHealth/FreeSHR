package org.freeshr.infrastructure.mci;

import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.dto.IsValidHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.concurrent.ExecutionException;

@Component
public class MasterClientIndexWrapper {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public MasterClientIndexWrapper(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    public ListenableFuture<Patient> getPatient(String healthId) {
        IsValidHealthId isValidHealthId = new IsValidHealthId();
        isValidHealthId.setHealthId(healthId);
        return new ListenableFutureAdapter<Patient, ResponseEntity<Patient>>(shrRestTemplate.postForEntity(shrProperties.getMCIUrl() + "/isValid", new HttpEntity<Object>(isValidHealthId), Patient.class)) {
            @Override
            protected Patient adapt(ResponseEntity<Patient> result) throws ExecutionException {
                if (result.getStatusCode().is2xxSuccessful()) {
                    return result.getBody();
                } else {
                    return null;
                }
            }
        };
    }
}
