package org.freeshr.infrastructure.tr;


import org.freeshr.config.SHRProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;

import static org.freeshr.utils.CollectionUtils.fetch;
import static org.freeshr.utils.HttpUtil.basicAuthHeaders;

public class HttpCodeValidator implements CodeValidator {

    public static final String REF_TERM_PATTERN = "/openmrs/ws/rest/v1/tr/referenceterms/";
    public static final String CONCEPT_PATTERN = "/openmrs/ws/rest/v1/tr/concepts/";

    private String path;
    private final AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    public HttpCodeValidator(AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties, String path) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
        this.path = path;
    }

    private Observable<ResponseEntity<Map>> get(String uri) {
        return Observable.from(shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                Map.class));
    }


    @Override
    public Observable<Boolean> isValid(String uri, final String code) {
        Observable<Boolean> observable = get(uri).map(new Func1<ResponseEntity<Map>, Boolean>() {
            @Override
            public Boolean call(ResponseEntity<Map> mapResponseEntity) {
                return mapResponseEntity.getStatusCode().is2xxSuccessful()
                        && fetch(mapResponseEntity.getBody(), path).equals(code);
            }
        });
        return observable.onErrorReturn(new Func1<Throwable, Boolean>() {
            @Override
            public Boolean call(Throwable throwable) {
                return false;
            }
        });
    }

}
