package org.freeshr.validations;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.validation.FhirInstanceValidator;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.ValidationSupportChain;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.TRConceptValidator;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.resource.ShrProfileValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FhirResourceValidator {

    private FhirFeedUtil fhirUtil;
    private TRConceptValidator trConceptValidator;
    private volatile FhirValidator fhirValidator;
    private ShrProfileValidationSupport shrProfileValidationSupport;
    private List<String> resourceFieldErrors = new ArrayList<>();
    private Map<String, String> extensionFieldErrors = new HashMap<>();

    @Autowired
    public FhirResourceValidator(FhirFeedUtil fhirUtil, TRConceptValidator trConceptValidator, ShrProfileValidationSupport shrProfileValidationSupport) {
        this.fhirUtil = fhirUtil;
        this.trConceptValidator = trConceptValidator;
        this.shrProfileValidationSupport = shrProfileValidationSupport;
        initFieldErrorChecks();
    }

    private void initFieldErrorChecks() {
        this.resourceFieldErrors.add("/f:Bundle/f:entry/f:resource/f:Condition/f:category");
        this.resourceFieldErrors.add("/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding");
        this.resourceFieldErrors.add("/f:Bundle/f:entry/f:resource/f:Condition/f:clinicalStatus");

        this.extensionFieldErrors.put("/f:Bundle/f:entry/f:resource/f:MedicationOrder/f:dosageInstruction/f:timing/f:extension", "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#TimingScheduledDate");
        this.extensionFieldErrors.put("/f:Bundle/f:entry/f:resource/f:MedicationOrder/f:dosageInstruction/f:extension", "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#DosageInstructionCustomDosage");
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
        checkForExtensionErrors(validationResult);
    }

    private void checkForExtensionErrors(FhirValidationResult validationResult) {
        for (SingleValidationMessage validationMessage : validationResult.getMessages()) {
            if (extensionFieldErrors.containsKey(validationMessage.getLocationString())
                    && validationMessage.getMessage().contains(extensionFieldErrors.get(validationMessage.getLocationString()))) {
                if (validationMessage.getSeverity().ordinal() >= ResultSeverityEnum.ERROR.ordinal()) {
                    validationMessage.setSeverity(ResultSeverityEnum.WARNING);
                }
            }
        }
    }

    private void checkForConditionErrors(FhirValidationResult validationResult) {
        for (SingleValidationMessage validationMessage : validationResult.getMessages()) {
            if (resourceFieldErrors.contains(validationMessage.getLocationString())) {
                if (validationMessage.getSeverity().ordinal() <= ResultSeverityEnum.WARNING.ordinal()) {
                    validationMessage.setSeverity(ResultSeverityEnum.ERROR);
                }
            }
        }
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
                if (trConceptValidator.isCodeSystemSupported(terminologySystem)) {
                    validationMessage.setSeverity(ResultSeverityEnum.ERROR);
                }
            }
        }
    }

    private static String getTerminologySystem(String message) {
        if (message.contains("Unable to validate code")) {
            String substring = message.substring(message.indexOf("in code system"));
            return StringUtils.remove(StringUtils.removeStart(substring, "in code system"), "\"");
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
