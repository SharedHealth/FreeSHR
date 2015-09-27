package org.freeshr.validations;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.validation.*;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.TRValidationSupport;
import org.freeshr.utils.FhirFeedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirResourceValidator {

    private FhirFeedUtil fhirUtil;
    private TRValidationSupport trValidationSupport;
    private volatile FhirValidator fhirValidator;

    @Autowired
    public FhirResourceValidator(FhirFeedUtil fhirUtil, TRValidationSupport trValidationSupport) {
        this.fhirUtil = fhirUtil;
        this.trValidationSupport = trValidationSupport;
    }

    public ValidationResult validateWithResult(Bundle bundle) {
        ValidationResult validationResult = validatorInstance().validateWithResult(bundle);
        return checkForConceptValidationError(validationResult);
    }

    /**
     * This is required since the InstanceValidator does not raise a severity.error on concept validation failure.
     * InstanceValidator.checkCodeableConcept() line number 225
     * @param validationResult
     * @return
     */
    private ValidationResult checkForConceptValidationError(ValidationResult validationResult) {
        for (SingleValidationMessage singleValidationMessage : validationResult.getMessages()) {
            String message = singleValidationMessage.getMessage();
            String terminologySystem = getTerminologySystem(message);
            if (!StringUtils.isBlank(terminologySystem)) {
                if (trValidationSupport.isCodeSystemSupported(terminologySystem)) {
                    singleValidationMessage.setSeverity(ResultSeverityEnum.ERROR);
                }
            }
        }
        return new ValidationResult(fhirUtil.getFhirContext(),validationResult.getMessages());
    }

    private static String getTerminologySystem(String message) {
        if (message.contains("Unable to validate code")) {
            String substring = message.substring(message.indexOf("in code system"));
            return StringUtils.remove(StringUtils.removeStart(substring, "in code system"),"\"");
        }
        return "";
    }

    private FhirValidator validatorInstance() {
        if (fhirValidator == null) {
            synchronized (FhirValidator.class) {
                if (fhirValidator == null) {
                    FhirValidator validator = fhirUtil.getFhirContext().newValidator();
                    validator.registerValidatorModule(validatorModule());
                    fhirValidator = validator;
                }
            }
        }
        return fhirValidator;
    }

    private IValidatorModule validatorModule() {
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        instanceValidator.setValidationSupport(validationSupportChain());
        return instanceValidator;
    }

    private ValidationSupportChain validationSupportChain() {
        return new ValidationSupportChain(new DefaultProfileValidationSupport(), trValidationSupport);
    }

}
