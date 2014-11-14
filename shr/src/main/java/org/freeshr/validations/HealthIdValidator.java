package org.freeshr.validations;


import org.freeshr.application.fhir.*;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.*;
import org.springframework.stereotype.Component;

@Component
public class HealthIdValidator {

    private final ResourceOrFeedDeserializer resourceOrFeedDeserializer;

    public HealthIdValidator() {
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public EncounterValidationResponse validate(String sourceXml, String expectedHealthId) {
        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(sourceXml);
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            Property subject = atomEntry.getResource().getChildByName("subject");
            if (!subject.hasValues()) continue;

            String healthId = ((ResourceReference) subject.getValues().get(0)).getReferenceSimple();
            if (healthId.equalsIgnoreCase(expectedHealthId)) continue;

            org.freeshr.application.fhir.Error error = new org.freeshr.application.fhir.Error("healthId", "invalid", "Patient's Health Id does not match.");
            encounterValidationResponse.addError(error);
            return encounterValidationResponse;
        }

        return encounterValidationResponse;
    }
}
