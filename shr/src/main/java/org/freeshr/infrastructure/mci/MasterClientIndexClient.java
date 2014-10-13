package org.freeshr.infrastructure.mci;

import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.patient.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.HttpUtil.basicAuthHeaders;

@Component
public class MasterClientIndexClient {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public MasterClientIndexClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    public ListenableFuture<Patient> getPatient(String healthId) {
        return new ListenableFutureAdapter<Patient, ResponseEntity<Patient>>(shrRestTemplate.exchange(
                shrProperties.getMCIPatientUrl() + "/" + healthId,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getMciUser(), shrProperties.getMciPassword())),
                Patient.class)) {
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
