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

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.freeshr.utils.HttpUtil.basicAuthHeaders;
import static org.springframework.util.StringUtils.isEmpty;

@Component
public class MedicationCodeValidator implements CodeValidator {

    public static final String MEDICATION_URL_PATTERN = "/openmrs/ws/rest/v1/tr/drugs/";

    private final AsyncRestTemplate shrRestTemplate;
    private final SHRProperties shrProperties;

    @Autowired
    public MedicationCodeValidator(AsyncRestTemplate shrRestTemplate,
                                   SHRProperties shrProperties) {

        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    @Override
    public Observable<Boolean> isValid(String uri, String code) {
        if (isEmpty(code) || substringAfterLast(uri, "/").equalsIgnoreCase(code)) {
            String medicationReferenceUrl = formMedicationReferenceUrl(uri);
            Observable<Boolean> map = get(medicationReferenceUrl).map(new Func1<ResponseEntity<String>, Boolean>() {
                @Override
                public Boolean call(ResponseEntity<String> response) {
                    return !response.getBody().isEmpty();
                }

            });
            return map.onErrorReturn(new Func1<Throwable, Boolean>() {
                @Override
                public Boolean call(Throwable throwable) {
                    return false;
                }
            });
        }
        return Observable.just(Boolean.FALSE);
    }

    private String formMedicationReferenceUrl(String uri) {
        String terminologyServerReferencePath = StringUtils.ensureEndsWithBackSlash(shrProperties.getTerminologyServerReferencePath());
        String trLocationPath = StringUtils.ensureEndsWithBackSlash(shrProperties.getTRLocationPath());
        return uri.replace(terminologyServerReferencePath, trLocationPath);
    }

    private Observable<ResponseEntity<String>> get(String uri) {
        return Observable.from(shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                String.class));
    }
}
