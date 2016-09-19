package org.freeshr.infrastructure;

import org.freeshr.config.SHRProperties;
import org.freeshr.interfaces.encounter.ws.FacilityMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.HttpUtil.getSHRIdentityHeaders;


@Component
public class ProviderRegistryClient {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public ProviderRegistryClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate,
                                  SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrRestTemplate.getMessageConverters().add(0, new FacilityMessageConverter());
        this.shrProperties = shrProperties;
    }

    @Cacheable(value = "prCache", unless = "#result == null")
    public boolean checkProvider(final String providerURL) throws ExecutionException, InterruptedException {

        ListenableFuture<ResponseEntity<String>> future = shrRestTemplate.exchange(
                providerURL,
                HttpMethod.GET,
                new HttpEntity(getSHRIdentityHeaders(shrProperties)),
                String.class);
        ResponseEntity<String> responseEntity = future.get();
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

}
