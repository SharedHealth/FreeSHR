package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;

@Component
public class CodeValidatorFactory {

    private static final String REF_TERM_PATTERN = "/openmrs/ws/rest/v1/tr/referenceterms/";
    private static final String CONCEPT_PATTERN = "/openmrs/ws/rest/v1/tr/concepts/";
    private static final String HL7_PATTERN = "http://hl7.org/fhir/";

    private CodeValidator hl7Validator;
    private CodeValidator refTermValidator;
    private CodeValidator conceptValidator;

    @Autowired
    public CodeValidatorFactory(AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties, CodeValidator hl7Validator) {
        this.hl7Validator = hl7Validator;
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
        if (url.contains(HL7_PATTERN)) {
            return hl7Validator;
        }
        return null;
    }
}
