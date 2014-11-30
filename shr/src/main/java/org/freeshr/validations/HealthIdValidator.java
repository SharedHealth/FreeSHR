package org.freeshr.validations;


import org.freeshr.application.fhir.*;
import org.freeshr.application.fhir.Error;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.*;
import org.springframework.stereotype.Component;

@Component
public class HealthIdValidator {

    private final ResourceOrFeedDeserializer resourceOrFeedDeserializer;

    public HealthIdValidator() {
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    public EncounterValidationResponse validate(AtomFeed feed, String expectedHealthId) {
        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            ResourceType resourceType = atomEntry.getResource().getResourceType();
            Property subject = atomEntry.getResource().getChildByName("subject");

            if(resourceType.equals(ResourceType.Composition) && !subject.hasValues()){
                org.freeshr.application.fhir.Error error = new org.freeshr.application.fhir.Error("healthId", "invalid", "Composition must have patient's Health Id in subject.");
                encounterValidationResponse.addError(error);
                return encounterValidationResponse;
            }

            if (!subject.hasValues()) continue;
            String healthIdFromUrl = getHealthIdFromUrl(((ResourceReference) subject.getValues().get(0)).getReferenceSimple());
            if (expectedHealthId.equals(healthIdFromUrl)) continue;

            Error error = new Error("healthId", "invalid", "Patient's Health Id does not match.");
            encounterValidationResponse.addError(error);
        }

        return encounterValidationResponse;
    }

    private String getHealthIdFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length());
    }
}
