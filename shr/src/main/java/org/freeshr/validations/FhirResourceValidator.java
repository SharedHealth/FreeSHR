package org.freeshr.validations;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.validation.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirInstanceValidator;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.ValidationSupportChain;
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
        return validatorInstance().validateWithResult(bundle);
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
