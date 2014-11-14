package org.freeshr.application.fhir;


import org.freeshr.validations.FhirSchemaValidator;
import org.freeshr.validations.ResourceValidator;
import org.freeshr.validations.HealthIdValidator;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.utils.CollectionUtils.reduce;

@Component
public class EncounterValidator {

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
    }

    public EncounterValidationResponse validate(EncounterBundle encounterBundle) {
            String sourceXml = encounterBundle.getEncounterContent().toString();

            EncounterValidationResponse encounterValidationResponse = healthIdValidator.validate(sourceXml, encounterBundle.getHealthId());

            return encounterValidationResponse.isSuccessful() ? validate(sourceXml) : encounterValidationResponse;
    }

    private EncounterValidationResponse validate(String sourceXml) {
        List<ValidationMessage> validationMessages = new ArrayList<>();

        validationMessages.addAll(fhirSchemaValidator.validate(sourceXml));
        validationMessages.addAll(resourceValidator.validateCategories(sourceXml));

        return fhirMessageFilter.filterMessagesSevereThan(validationMessages, OperationOutcome.IssueSeverity.warning);
    }

}
