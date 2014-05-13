package org.freeshr.shr.patient.wrapper;

import org.freeshr.shr.patient.wrapper.request.IsValidHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MasterClientIndexWrapper {

    private RestTemplate shrRestTemplate;

    @Autowired
    public MasterClientIndexWrapper(@Qualifier("SHRRestTemplate") RestTemplate shrRestTemplate) {
        this.shrRestTemplate = shrRestTemplate;
    }

    public Boolean isValid(String healthId) {
        IsValidHealthId isValidHealthId = new IsValidHealthId();
        isValidHealthId.setHealthId(healthId);
        try {
            shrRestTemplate.postForLocation("http://localhost:9999/isValid", isValidHealthId);
            return Boolean.TRUE;
        } catch (RestClientException e) {
            return Boolean.FALSE;
        }
    }
}
