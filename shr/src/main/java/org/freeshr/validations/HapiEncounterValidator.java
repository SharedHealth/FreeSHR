package org.freeshr.validations;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.freeshr.application.fhir.*;
import org.freeshr.application.fhir.Error;
import org.freeshr.utils.FhirFeedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("hapiEncounterValidator")
public class HapiEncounterValidator implements ShrEncounterValidator {

    private FhirFeedUtil feedUtil;

    @Autowired
    public HapiEncounterValidator(FhirFeedUtil feedUtil) {
        this.feedUtil = feedUtil;
    }
    @Override
    public EncounterValidationResponse validate(EncounterValidationContext validationContext) {
        EncounterValidationResponse validationResponse = new EncounterValidationResponse();
        Bundle bundle = validationContext.getBundle();
        ValidationResult validationResult = feedUtil.getFhirContext().newValidator().validateWithResult(bundle);

        if (!validationResult.isSuccessful()) {
            for (SingleValidationMessage validationMessage : validationResult.getMessages()) {
                Error error = new Error(validationMessage.getLocationString(), validationMessage.getSeverity().getCode(), validationMessage.getMessage());
                validationResponse.addError(error);
            }
        }
        validationResponse.setBundle(bundle);
        return validationResponse;
    }
}
