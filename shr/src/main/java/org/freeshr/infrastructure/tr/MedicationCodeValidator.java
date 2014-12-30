package org.freeshr.infrastructure.tr;

import org.apache.commons.io.IOUtils;
import org.freeshr.config.SHRProperties;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Medication;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import static org.freeshr.utils.HttpUtil.basicAuthHeaders;

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
        Observable<Boolean> map = get(uri).map(new Func1<ResponseEntity<String>, Boolean>() {
            @Override
            public Boolean call(ResponseEntity<String> response) {
//                Medication medication = deserializeToMedication(response.getBody());
//                return medication != null;
                //NOTE:avoiding deserialization. simply check if there are any contents.
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

    private Medication deserializeToMedication(String responseBody) {
        Resource resource = null;
        try {
            resource = new JsonParser().parse(IOUtils.toInputStream(responseBody, "UTF-8"));
            return (Medication) resource;
        } catch (Exception e) {
            //do nothing!!
        }
        return null;
    }

    private Observable<ResponseEntity<String>> get(String uri) {
        return Observable.from(shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                String.class));
    }
}
