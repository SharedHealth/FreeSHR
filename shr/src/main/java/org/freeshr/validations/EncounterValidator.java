package org.freeshr.validations;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirMessageFilter;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.freeshr.application.fhir.EncounterValidationResponse.createErrorResponse;
import static org.freeshr.application.fhir.EncounterValidationResponse.fromValidationMessages;

@Component
public class EncounterValidator {

    private FhirMessageFilter fhirMessageFilter;
    private FhirSchemaValidator fhirSchemaValidator;
    private ResourceValidator resourceValidator;
    private HealthIdValidator healthIdValidator;
    private StructureValidator structureValidator;
    private FacilityValidator facilityValidator;
    private ProviderValidator providerValidator;

    @Autowired
    public EncounterValidator(FhirMessageFilter fhirMessageFilter,
                              FhirSchemaValidator fhirSchemaValidator,
                              ResourceValidator resourceValidator,
                              HealthIdValidator healthIdValidator,
                              StructureValidator structureValidator,
                              ProviderValidator providerValidator,
                              FacilityValidator facilityValidator) {
        this.fhirMessageFilter = fhirMessageFilter;
        this.fhirSchemaValidator = fhirSchemaValidator;
        this.resourceValidator = resourceValidator;
        this.healthIdValidator = healthIdValidator;
        this.structureValidator = structureValidator;
        this.facilityValidator = facilityValidator;
        this.providerValidator = providerValidator;
    }

    public EncounterValidationResponse validate(EncounterValidationContext validationContext) {
        try {
            EncounterValidationResponse validationResponse = fromValidationMessages(
                    fhirSchemaValidator.validate(validationContext.sourceFragment()), fhirMessageFilter);
            if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse = fromValidationMessages(
                    structureValidator.validate(validationContext.feedFragment()), fhirMessageFilter);
            if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse.mergeErrors(fromValidationMessages(
                    facilityValidator.validate(validationContext.feedFragment()), fhirMessageFilter));
            //if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse.mergeErrors(fromValidationMessages(
                    providerValidator.validate(validationContext.feedFragment()), fhirMessageFilter));
            //if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse.mergeErrors(fromValidationMessages(
                    resourceValidator.validate(validationContext.feedFragment()), fhirMessageFilter));


            return validationResponse.isSuccessful()
                    ? fromValidationMessages(healthIdValidator.validate(validationContext.context()), fhirMessageFilter)
                    : validationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(e);
        }
    }


}
