package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.HashMap;
import java.util.Map;


@Component
public class CodeValidatorFactory {

    private Map<String, CodeValidator> codeValidatorMap;

    @Autowired
    public CodeValidatorFactory(AsyncRestTemplate shrRestTemplate,
                                SHRProperties shrProperties,
                                ValueSetCodeValidator valueSetCodeValidator,
                                MedicationCodeValidator medicationCodeValidator) {
        codeValidatorMap = new HashMap<>();
        codeValidatorMap.put(shrProperties.getReferenceTermContextPath(), new HttpCodeValidator(shrRestTemplate, shrProperties));
        codeValidatorMap.put(shrProperties.getInterfaceTermContextPath(), new HttpCodeValidator(shrRestTemplate, shrProperties));
        codeValidatorMap.put(shrProperties.getTerminologiesContextPathForValueSet(), valueSetCodeValidator);
        codeValidatorMap.put(shrProperties.getTerminologiesContextPathForMedication(), medicationCodeValidator);
    }


    public CodeValidator getValidator(String systemUrl) {
        for (String urlKey : codeValidatorMap.keySet()) {
            if (systemUrl.contains(urlKey)) {
                return codeValidatorMap.get(urlKey);
            }
        }
        return null;
    }

}
