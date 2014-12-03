package org.freeshr.application.fhir;


import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.freeshr.validations.FhirSchemaValidator;
import org.freeshr.validations.HealthIdValidator;
import org.freeshr.validations.ResourceValidator;
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

    @Autowired
    public EncounterValidator(FhirMessageFilter fhirMessageFilter, FhirSchemaValidator fhirSchemaValidator, ResourceValidator resourceValidator, HealthIdValidator healthIdValidator) {
        this.fhirMessageFilter = fhirMessageFilter;
        this.fhirSchemaValidator = fhirSchemaValidator;
        this.resourceValidator = resourceValidator;
        this.healthIdValidator = healthIdValidator;
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public EncounterValidationResponse validate(EncounterBundle encounterBundle) {
        String sourceXml = encounterBundle.getEncounterContent().toString();

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        AtomFeed feed = null;
        try {
            feed = resourceOrFeedDeserializer.deserialize(sourceXml);
        } catch (Exception e) {
            org.freeshr.application.fhir.Error error = new org.freeshr.application.fhir.Error("Condition-status", "invalid", e.getMessage());
            encounterValidationResponse.addError(error);
            return encounterValidationResponse;
        }
        encounterValidationResponse = healthIdValidator.validate(feed, encounterBundle.getHealthId());
        return encounterValidationResponse.isSuccessful() ? validate(sourceXml, feed) : encounterValidationResponse;

    }

    private EncounterValidationResponse validate(String sourceXml, AtomFeed feed) {
        List<ValidationMessage> validationMessages = new ArrayList<>();

        validationMessages.addAll(fhirSchemaValidator.validate(sourceXml));
        validationMessages.addAll(resourceValidator.validate(feed));

        return fhirMessageFilter.filterMessagesSevereThan(validationMessages, OperationOutcome.IssueSeverity.warning);
    }

}
