package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Map;

import static org.freeshr.utils.CollectionUtils.fetch;
import static org.freeshr.utils.HttpUtil.basicAuthHeaders;

@Component
public class TerminologyServer {

    private final AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;


    @Autowired
    public TerminologyServer(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }


    private void get(String uri, ListenableFutureCallback<ResponseEntity<Map>> callback) {
        ListenableFuture<ResponseEntity<Map>> future = shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                Map.class);
        future.addCallback(callback);
    }

    private SettableListenableFuture<Boolean> hasCode(String uri, final String path, final String code) {
        final SettableListenableFuture<Boolean> future = new SettableListenableFuture<Boolean>();

        get(uri, new ListenableFutureCallback<ResponseEntity<Map>>() {
            public void onSuccess(ResponseEntity<Map> result) {
                future.set(result.getStatusCode().is2xxSuccessful() && fetch(result.getBody(), path).equals(code));
            }

            public void onFailure(Throwable t) {
                future.set(false);
            }
        });

        return future;
    }

    public ListenableFuture<Boolean> isValidReferenceTerm(String uri, final String code) {
        return hasCode(uri, "conceptSource.hl7Code", code);
    }

    public ListenableFuture<Boolean> isValidConcept(String uri, final String code) {
        return hasCode(uri, "uuid", code);
    }

    public ListenableFuture<Boolean> isValid(String uri, String code) {
        return uri.contains("/conceptreferenceterm/") ? isValidReferenceTerm(uri, code) : isValidConcept(uri, code);
    }
}
