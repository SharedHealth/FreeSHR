package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;

@Component
public class CodeValidatorFactory {

    private static final String REF_TERM_PATTERN = "/openmrs/ws/rest/v1/tr/referenceterms/";
    private static final String CONCEPT_PATTERN = "/openmrs/ws/rest/v1/tr/concepts/";

    private CodeValidator refTermValidator;
    private CodeValidator conceptValidator;

    @Autowired
    public CodeValidatorFactory(AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.refTermValidator = new HttpCodeValidator(shrRestTemplate, shrProperties, "code");
        this.conceptValidator = new HttpCodeValidator(shrRestTemplate, shrProperties, "uuid");
    }


    public CodeValidator getValidator(String url) {
        if (url.contains(REF_TERM_PATTERN)) {
            return refTermValidator;
        }
        if (url.contains(CONCEPT_PATTERN)) {
            return conceptValidator;
        }
        return null;
    }
}
