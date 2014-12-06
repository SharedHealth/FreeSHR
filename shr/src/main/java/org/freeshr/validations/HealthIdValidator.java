package org.freeshr.validations;


import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HealthIdValidator implements Validator<EncounterValidationContext> {

    public HealthIdValidator() {
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<EncounterValidationContext> fragment) {
        EncounterValidationContext validationContext = fragment.extract();
        AtomFeed feed = validationContext.getFeed();
        String expectedHealthId = validationContext.getHealthId();
        List<ValidationMessage> validationMessages = new ArrayList<>();
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            ResourceType resourceType = atomEntry.getResource().getResourceType();
            Property subject = atomEntry.getResource().getChildByName("subject");
            if (resourceType.equals(ResourceType.Composition) && !subject.hasValues()) {
                ValidationMessage validationMessage = createValidationMessage("healthId", "invalid",
                        "Composition must have patient's Health Id in subject.");
                validationMessages.add(validationMessage);
                return validationMessages;
            }
            if (!subject.hasValues()) continue;
            String healthIdFromUrl = getHealthIdFromUrl(((ResourceReference) subject.getValues().get(0))
                    .getReferenceSimple());
            if (expectedHealthId.equals(healthIdFromUrl)) continue;
            ValidationMessage validationMessage = createValidationMessage("healthId", "invalid",
                    "Patient's Health Id does not match.");
            validationMessages.add(validationMessage);
        }

        return validationMessages;
    }

    private ValidationMessage createValidationMessage(String location, String type, String message) {
        ValidationMessage validationMessage = new ValidationMessage();
        validationMessage.setLocation(location);
        validationMessage.setType(type);
        validationMessage.setMessage(message);
        validationMessage.setLevel(OperationOutcome.IssueSeverity.error);
        return validationMessage;
    }

    private String getHealthIdFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length());
    }
}
