package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.freeshr.utils.HttpUtil.basicAuthHeaders;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class MedicationCodeValidator implements CodeValidator {

    private final AsyncRestTemplate shrRestTemplate;
    private final SHRProperties shrProperties;

    @Autowired
    public MedicationCodeValidator(AsyncRestTemplate shrRestTemplate,
                                   SHRProperties shrProperties) {

        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    @Override
    public boolean supports(String system) {
        return (system != null) && system.contains(shrProperties.getTerminologiesContextPathForMedication());
    }

    @Override
    public Observable<Boolean> isValid(String system, String code) {
        if (isEmpty(code) || substringAfterLast(system, "/").equalsIgnoreCase(code)) {
            Observable<Boolean> map = fetch(system).map(new Func1<ResponseEntity<String>, Boolean>() {
                @Override
                public Boolean call(ResponseEntity<String> response) {
                    return !response.getBody().isEmpty();
                }

            });
            map.onErrorReturn(new Func1<Throwable, Boolean>() {
                @Override
                public Boolean call(Throwable throwable) {
                    return false;
                }
            });
            return map;
        }
        return Observable.just(Boolean.FALSE);
    }

    private Observable<ResponseEntity<String>> fetch(String uri) {
        return Observable.from(shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                String.class));
    }
}
