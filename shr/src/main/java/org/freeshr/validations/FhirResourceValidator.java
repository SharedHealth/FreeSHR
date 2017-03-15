package org.freeshr.validations;

import ca.uhn.fhir.validation.*;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.TRConceptValidator;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.resource.ShrProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FhirResourceValidator {

    private FhirFeedUtil fhirUtil;
    private TRConceptValidator trConceptValidator;
    private volatile FhirValidator fhirValidator;
    private ShrProfileValidationSupport shrProfileValidationSupport;
    private List<Pattern> resourceFieldErrors = new ArrayList<>();

    @Autowired
    public FhirResourceValidator(FhirFeedUtil fhirUtil, TRConceptValidator trConceptValidator, ShrProfileValidationSupport shrProfileValidationSupport) {
        this.fhirUtil = fhirUtil;
        this.trConceptValidator = trConceptValidator;
        this.shrProfileValidationSupport = shrProfileValidationSupport;
        initFieldErrorChecks();
    }

    private void initFieldErrorChecks() {
        this.resourceFieldErrors.add(Pattern.compile("Bundle\\.entry(\\[\\d+\\])*\\.resource\\.category\\.coding"));
        this.resourceFieldErrors.add(Pattern.compile("Bundle\\.entry(\\[\\d+\\])*\\.resource\\.code\\.coding"));
    }

    public FhirValidationResult validate(Bundle bundle) {
        ValidationResult result = validatorInstance().validateWithResult(bundle);
        FhirValidationResult validationResult = new FhirValidationResult(fhirUtil.getFhirContext(), result);
        checkValidationResult(validationResult);
        return validationResult;
    }

    private void checkValidationResult(FhirValidationResult validationResult) {
        checkForConceptValidationError(validationResult);
        checkForConditionErrors(validationResult);
    }

    private void checkForConditionErrors(FhirValidationResult validationResult) {
        for (SingleValidationMessage validationMessage : validationResult.getMessages()) {
            if (isPossibleResourceFieldError(validationMessage.getLocationString())) {
                if (validationMessage.getSeverity().ordinal() <= ResultSeverityEnum.WARNING.ordinal()) {
                    validationMessage.setSeverity(ResultSeverityEnum.ERROR);
                }
            }
        }
    }

    private boolean isPossibleResourceFieldError(String locationString) {
        for (Pattern resourceFieldErrorPattern : resourceFieldErrors) {
            Matcher matcher = resourceFieldErrorPattern.matcher(locationString);
            if (matcher.matches()) return true;
        }
        return false;
    }

    /**
     * This is required since the InstanceValidator does not raise a severity.error on concept validation failure.
     * InstanceValidator.checkCodeableConcept() line number 225
     */
    private void checkForConceptValidationError(FhirValidationResult validationResult) {
        for (SingleValidationMessage validationMessage : validationResult.getMessages()) {
            String message = validationMessage.getMessage();
            String terminologySystem = getTerminologySystem(message);
            if (!StringUtils.isBlank(terminologySystem)) {
                if (trConceptValidator.isCodeSystemSupported(fhirUtil.getFhirContext(), terminologySystem)) {
                    validationMessage.setSeverity(ResultSeverityEnum.ERROR);
                }
            }
        }
    }

    private static String getTerminologySystem(String message) {
        Pattern pattern = Pattern.compile("Unable to validate code \"(.*)\" in code system \"(?<TRSERVERURL>.*)\"");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            return matcher.group("TRSERVERURL");
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
        return new ValidationSupportChain(shrProfileValidationSupport, trConceptValidator);
    }

}
