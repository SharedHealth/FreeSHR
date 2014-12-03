package org.freeshr.validations;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirMessageFilter;
import org.freeshr.infrastructure.tr.ValueSetCodeValidator;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EncounterValidator {

    private final ResourceOrFeedDeserializer resourceOrFeedDeserializer;
    private FhirMessageFilter fhirMessageFilter;
    private FhirSchemaValidator fhirSchemaValidator;
    private ResourceValidator resourceValidator;
    private HealthIdValidator healthIdValidator;
    private ValueSetCodeValidator valueSetCodeValidator;

    @Autowired
    public EncounterValidator(FhirMessageFilter fhirMessageFilter,
                              FhirSchemaValidator fhirSchemaValidator,
                              ResourceValidator resourceValidator,
                              HealthIdValidator healthIdValidator,
                              ValueSetCodeValidator valueSetCodeValidator) {
        this.fhirMessageFilter = fhirMessageFilter;
        this.fhirSchemaValidator = fhirSchemaValidator;
        this.resourceValidator = resourceValidator;
        this.healthIdValidator = healthIdValidator;
        this.valueSetCodeValidator = valueSetCodeValidator;
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public EncounterValidationResponse validate(EncounterBundle encounterBundle) {
        String sourceXml = encounterBundle.getEncounterContent().toString();
        EncounterValidationResponse validationResponse = validateSchema(sourceXml);
        if (validationResponse.isNotSuccessful())
            return validationResponse;

        AtomFeed feed = null;
        try {
            feed = resourceOrFeedDeserializer.deserialize(sourceXml);
        } catch (Exception e) {
            return createErrorResponse(e);
        }

        validationResponse = validateResources(feed);
        return validationResponse.isSuccessful() ? healthIdValidator.validate(feed, encounterBundle.getHealthId())
                : validationResponse;
    }

    private EncounterValidationResponse validateSchema(String sourceXml) {
        List<ValidationMessage> validationMessages = fhirSchemaValidator.validate(sourceXml);
        return createValidationResponse(validationMessages);
    }

    private EncounterValidationResponse validateResources(AtomFeed feed) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        validationMessages.addAll(resourceValidator.validate(feed));
        return createValidationResponse(validationMessages);
    }

    private EncounterValidationResponse createErrorResponse(Exception e) {
        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.addError(new org.freeshr.application.fhir.Error("Condition-status", "invalid", e.getMessage()));
        return encounterValidationResponse;
    }


    private EncounterValidationResponse createValidationResponse(List<ValidationMessage> validationMessages) {
        return fhirMessageFilter.filterMessagesSevereThan(validationMessages,
                OperationOutcome.IssueSeverity.warning);
    }


}
