package org.freeshr.validations;


import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirMessageFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.freeshr.application.fhir.EncounterValidationResponse.createErrorResponse;
import static org.freeshr.application.fhir.EncounterValidationResponse.fromValidationMessages;

@Component("refImplEncounterValidator")
public class RIEncounterValidator implements ShrEncounterValidator {

    private FhirMessageFilter fhirMessageFilter;
    private FhirSchemaValidator fhirSchemaValidator;
    private ResourceValidator resourceValidator;
    private HealthIdValidator healthIdValidator;
    private StructureValidator structureValidator;
    private FacilityValidator facilityValidator;
    private ProviderValidator providerValidator;

    @Autowired
    public RIEncounterValidator(FhirMessageFilter fhirMessageFilter,
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
                    healthIdValidator.validate(validationContext.context()), fhirMessageFilter));

            validationResponse.mergeErrors(fromValidationMessages(
                    facilityValidator.validate(validationContext.feedFragment()), fhirMessageFilter));
            //if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse.mergeErrors(fromValidationMessages(
                    providerValidator.validate(validationContext.feedFragment()), fhirMessageFilter));
            //if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse.mergeErrors(fromValidationMessages(
                    resourceValidator.validate(validationContext.feedFragment()), fhirMessageFilter));

            if(validationResponse.isSuccessful()) {
                validationResponse.setFeed(validationContext.getFeed());
            }
            return validationResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(e);
        }
    }


}
