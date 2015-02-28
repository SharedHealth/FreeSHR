package org.freeshr.validations;


import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.*;

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
            Resource resource = atomEntry.getResource();
            ResourceType resourceType = resource.getResourceType();
            Property subject = resource.getChildByName("subject");

            if (subject == null) {
                subject = resource.getChildByName("patient");
            }

            if (subject == null) {
                validationMessages.add(new ValidationMessage(null, "invalid", "healthId", HEALTH_ID_NOT_PRESENT,
                        OperationOutcome
                                .IssueSeverity.error));
                continue;
            }

            if (resourceType.equals(ResourceType.Composition) && !subject.hasValues()) {
                validationMessages.add(new ValidationMessage(null, "invalid", "healthId",
                        HEALTH_ID_NOT_PRESENT_IN_COMPOSITION, OperationOutcome.IssueSeverity.error));
                return validationMessages;
            }
            if (!subject.hasValues()) continue;
            String healthIdFromUrl = getHealthIdFromUrl(((ResourceReference) subject.getValues().get(0))
                    .getReferenceSimple());
            if (expectedHealthId.equals(healthIdFromUrl)) continue;

            validationMessages.add(new ValidationMessage(null, "invalid", "healthId", HEALTH_ID_NOT_MATCH,
                    OperationOutcome.IssueSeverity.error));
        }
        return validationMessages;
    }

    private String getHealthIdFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length());
    }
}
