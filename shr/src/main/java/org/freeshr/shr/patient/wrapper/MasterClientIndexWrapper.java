package org.freeshr.shr.patient.wrapper;

import org.freeshr.shr.config.SHRProperties;
import org.freeshr.shr.patient.wrapper.request.IsValidHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import java.net.URI;

@Component
public class MasterClientIndexWrapper {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public MasterClientIndexWrapper(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    public ListenableFuture<URI> isValid(String healthId, ListenableFutureCallback<URI> callback) {
        IsValidHealthId isValidHealthId = new IsValidHealthId();
        isValidHealthId.setHealthId(healthId);
        ListenableFuture<URI> result = shrRestTemplate.postForLocation(shrProperties.getMCIUrl() + "/isValid", new HttpEntity<Object>(isValidHealthId));
        result.addCallback(callback);
        return result;
    }
}
