package org.freeshr.infrastructure.tr;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func0;
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

    private Observable<ResponseEntity<String>> fetch(String uri) {
        return Observable.from(shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                String.class));
    }

    @Override
    public Observable<Boolean> isValid(final String system, final String code) {
        if (!StringUtils.isBlank(system)) {
            return fetch(system).flatMap(new Func1<ResponseEntity<String>, Observable<Boolean>>() {
                @Override
                public Observable<Boolean> call(ResponseEntity<String> stringResponseEntity) {
                    boolean checkResult = checkMedicationCode(system, code, stringResponseEntity.getBody());
                    boolean result = isEmpty(code) || checkResult;
                    return Observable.just(result);
                }
            }, new Func1<Throwable, Observable<? extends Boolean>>() {
                @Override
                public Observable<? extends Boolean> call(Throwable throwable) {
                    return Observable.just(Boolean.FALSE);
                }
            }, new Func0<Observable<? extends Boolean>>() {
                @Override
                public Observable<? extends Boolean> call() {
                    return null;
                }
            });
        }
        return Observable.just(Boolean.FALSE);
    }

    private boolean checkMedicationCode(final String system, final String code, final String medicationJson) {
        //should be deserializing medication and checking code
        return substringAfterLast(system, "/").equalsIgnoreCase(code);
    }
}
