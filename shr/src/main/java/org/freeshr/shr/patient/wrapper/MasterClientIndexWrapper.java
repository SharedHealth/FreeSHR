package org.freeshr.shr.patient.wrapper;

import org.freeshr.shr.config.SHRProperties;
import org.freeshr.shr.patient.wrapper.request.IsValidHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
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

    public ListenableFuture<Boolean> isValid(String healthId) {
        IsValidHealthId isValidHealthId = new IsValidHealthId();
        isValidHealthId.setHealthId(healthId);
        return new ListenableFutureAdapter<Boolean, ResponseEntity<String>>(shrRestTemplate.postForEntity(shrProperties.getMCIUrl() + "/isValid", new HttpEntity<Object>(isValidHealthId), String.class)) {
            @Override
            protected Boolean adapt(ResponseEntity<String> result) throws ExecutionException {
                return HttpStatus.OK.equals(result.getStatusCode());
            }
        };
    }
}
