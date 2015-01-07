package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.freeshr.infrastructure.tr.MedicationCodeValidator.MEDICATION_URL_PATTERN;
import static org.freeshr.infrastructure.tr.ValueSetCodeValidator.VALUE_SET_PATTERN;

@Component
public class CodeValidatorFactory {

    private static final String REF_TERM_PATTERN = "/openmrs/ws/rest/v1/tr/referenceterms/";
    private static final String CONCEPT_PATTERN = "/openmrs/ws/rest/v1/tr/concepts/";


    private Map<String, CodeValidator> codeValidatorMap;

    @Autowired
    public CodeValidatorFactory(AsyncRestTemplate shrRestTemplate,
                                SHRProperties shrProperties,
                                ValueSetCodeValidator valueSetCodeValidator,
                                MedicationCodeValidator medicationCodeValidator) {
        codeValidatorMap = new HashMap<>();
        codeValidatorMap.put(REF_TERM_PATTERN,new HttpCodeValidator(shrRestTemplate, shrProperties, "code"));
        codeValidatorMap.put(CONCEPT_PATTERN,new HttpCodeValidator(shrRestTemplate, shrProperties, "uuid"));
        codeValidatorMap.put(VALUE_SET_PATTERN,valueSetCodeValidator);
        codeValidatorMap.put(MEDICATION_URL_PATTERN,medicationCodeValidator);
    }


    public CodeValidator getValidator(String url) {
        for (String urlKey : codeValidatorMap.keySet()) {
            if(url.contains(urlKey)){
                return codeValidatorMap.get(urlKey);
            }
        }
        return null;
    }
}
