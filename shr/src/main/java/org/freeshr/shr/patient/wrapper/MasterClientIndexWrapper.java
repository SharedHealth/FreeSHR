package org.freeshr.shr.patient.wrapper;

import org.freeshr.shr.config.SHRProperties;
import org.freeshr.shr.patient.wrapper.request.IsValidHealthId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MasterClientIndexWrapper {

    private RestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public MasterClientIndexWrapper(@Qualifier("SHRRestTemplate") RestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    public Boolean isValid(String healthId) {
        IsValidHealthId isValidHealthId = new IsValidHealthId();
        isValidHealthId.setHealthId(healthId);
        try {
            shrRestTemplate.postForLocation(shrProperties.getMCIUrl() + "/isValid", isValidHealthId);
            return Boolean.TRUE;
        } catch (RestClientException e) {
            return Boolean.FALSE;
        }
    }
}
