package org.freeshr.validations;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirMessageFilter;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.AtomFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.freeshr.application.fhir.EncounterValidationResponse.createErrorResponse;
import static org.freeshr.application.fhir.EncounterValidationResponse.fromValidationMessages;

@Component
public class EncounterValidator {

    private final ResourceOrFeedDeserializer resourceOrFeedDeserializer;
    private FhirMessageFilter fhirMessageFilter;
    private FhirSchemaValidator fhirSchemaValidator;
    private ResourceValidator resourceValidator;
    private HealthIdValidator healthIdValidator;
    private StructureValidator structureValidator;

    @Autowired
    public EncounterValidator(FhirMessageFilter fhirMessageFilter,
                              FhirSchemaValidator fhirSchemaValidator,
                              ResourceValidator resourceValidator,
                              HealthIdValidator healthIdValidator,
                              StructureValidator structureValidator) {
        this.fhirMessageFilter = fhirMessageFilter;
        this.fhirSchemaValidator = fhirSchemaValidator;
        this.resourceValidator = resourceValidator;
        this.healthIdValidator = healthIdValidator;
        this.structureValidator = structureValidator;
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public EncounterValidationResponse validate(EncounterBundle encounterBundle) {
        try {
            final EncounterValidationContext validationContext = new EncounterValidationContext(encounterBundle,
                    resourceOrFeedDeserializer);

            EncounterValidationResponse validationResponse = fromValidationMessages(fhirSchemaValidator.validate(
                    sourceXml(validationContext)), fhirMessageFilter);
            if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse = fromValidationMessages(structureValidator.validate(feed(validationContext)), fhirMessageFilter);
            if (validationResponse.isNotSuccessful()) return validationResponse;

            validationResponse = fromValidationMessages(resourceValidator.validate(feed(validationContext)), fhirMessageFilter);
            return validationResponse.isSuccessful() ? fromValidationMessages(healthIdValidator.validate(context(validationContext)), fhirMessageFilter)
                    : validationResponse;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    private EncounterValidationFragment<EncounterValidationContext> context(final EncounterValidationContext validationContext) {
        return new EncounterValidationFragment<EncounterValidationContext>() {
            @Override
            public EncounterValidationContext extract() {
                return validationContext;
            }
        };
    }

    private EncounterValidationFragment<AtomFeed> feed(final EncounterValidationContext validationContext) {
        return new EncounterValidationFragment<AtomFeed>() {
            @Override
            public AtomFeed extract() {
                return validationContext.getFeed();
            }
        };
    }

    private EncounterValidationFragment<String> sourceXml(final EncounterValidationContext validationContext) {
        return new EncounterValidationFragment<String>() {
            @Override
            public String extract() {
                return validationContext.getSourceXml();
            }
        };
    }


}
