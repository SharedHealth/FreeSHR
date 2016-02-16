package org.freeshr.validations;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.Error;
import org.freeshr.validations.bundle.BundleResourceValidator;
import org.freeshr.validations.bundle.FacilityValidator;
import org.freeshr.validations.bundle.HealthIdValidator;
import org.freeshr.validations.bundle.ProviderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.freeshr.application.fhir.EncounterValidationResponse.fromShrValidationMessages;

@Component("hapiEncounterValidator")
public class HapiEncounterValidator implements ShrEncounterValidator {

    private FhirResourceValidator fhirResourceValidator;
    private HealthIdValidator healthIdValidator;
    private FacilityValidator facilityValidator;
    private ProviderValidator providerValidator;
    private BundleResourceValidator bundleResourceValidator;

    @Autowired
    public HapiEncounterValidator(FhirResourceValidator fhirResourceValidator,
                                  HealthIdValidator healthIdValidator,
                                  FacilityValidator facilityValidator,
                                  ProviderValidator providerValidator,
                                  BundleResourceValidator bundleResourceValidator) {
        this.fhirResourceValidator = fhirResourceValidator;
        this.healthIdValidator = healthIdValidator;
        this.facilityValidator = facilityValidator;
        this.providerValidator = providerValidator;
        this.bundleResourceValidator = bundleResourceValidator;
    }
    @Override
    public EncounterValidationResponse validate(EncounterValidationContext validationContext) {
        EncounterValidationResponse validationResponse = new EncounterValidationResponse();
        Bundle bundle = validationContext.getBundle();
        FhirValidationResult validationResult = fhirResourceValidator.validate(bundle);
        if (!validationResult.isSuccessful()) {
            return respondFromValidationMessages(validationResult);
        }

        validationResponse.mergeErrors(fromShrValidationMessages(
                healthIdValidator.validate(validationContext.context())));

        validationResponse.mergeErrors(fromShrValidationMessages(
                facilityValidator.validate(validationContext.bundleFragment())));

        validationResponse.mergeErrors(fromShrValidationMessages(
                providerValidator.validate(validationContext.bundleFragment())));

        validationResponse.mergeErrors(fromShrValidationMessages(
                bundleResourceValidator.validate(validationContext.bundleFragment())));

        if(validationResponse.isSuccessful()) {
            validationResponse.setBundle(bundle);
        }
        return validationResponse;
    }

    @Override
    public String supportedVersion() {
        return "v2";
    }

    private EncounterValidationResponse respondFromValidationMessages(ValidationResult validationResult) {
        EncounterValidationResponse response = new EncounterValidationResponse();
        for (SingleValidationMessage validationMessage : validationResult.getMessages()) {
            boolean possibleError = validationMessage.getSeverity().compareTo(ResultSeverityEnum.ERROR) >= 0;
            if (possibleError) {
                response.addError(new Error(validationMessage.getLocationString(), validationMessage.getSeverity().getCode(), validationMessage.getMessage()));
            }
        }
        return response;

//        for (SingleValidationMessage validationMessage : validationResult.getMessages()) {
//            Error error = new Error(validationMessage.getLocationString(), validationMessage.getSeverity().getCode(), validationMessage.getMessage());
//            validationResponse.addError(error);
//        }
//        return validationResponse;
    }


}
