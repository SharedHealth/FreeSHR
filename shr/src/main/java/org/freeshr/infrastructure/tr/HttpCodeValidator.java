package org.freeshr.infrastructure.tr;


import org.freeshr.config.SHRProperties;
import org.freeshr.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;

import static org.freeshr.utils.CollectionUtils.fetch;
import static org.freeshr.utils.HttpUtil.basicAuthHeaders;

@Component
public class HttpCodeValidator implements CodeValidator {

    private final AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public HttpCodeValidator(AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    private Observable<ResponseEntity<Map>> get(String uri) {
        return Observable.from(shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                Map.class));
    }


    @Override
    public boolean supports(String system) {
        if (system == null) return false;
        return getCodeAttributeToSearchFor(system) != null;
    }

    @Override
    public Observable<Boolean> isValid(String system, final String code) {
        final String attribute = getCodeAttributeToSearchFor(system);
        Observable<Boolean> observable = get(system).map(new Func1<ResponseEntity<Map>, Boolean>() {
            @Override
            public Boolean call(ResponseEntity<Map> mapResponseEntity) {
                return mapResponseEntity.getStatusCode().is2xxSuccessful()
                        && fetch(mapResponseEntity.getBody(), attribute).equals(code);
            }
        });
        return observable.onErrorReturn(new Func1<Throwable, Boolean>() {
            @Override
            public Boolean call(Throwable throwable) {
                return false;
            }
        });
    }

    private String getCodeAttributeToSearchFor(String system) {
        if (system.contains(shrProperties.getReferenceTermContextPath())) {
            return "code";
        }

        if (system.contains(shrProperties.getInterfaceTermContextPath())) {
            return "uuid";
        }

        return null;
    }
}
