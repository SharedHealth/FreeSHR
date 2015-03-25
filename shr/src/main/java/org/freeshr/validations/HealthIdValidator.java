package org.freeshr.validations;


import org.freeshr.config.SHRProperties;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.HEALTH_ID_NOT_MATCH;
import static org.freeshr.validations.ValidationMessages.HEALTH_ID_NOT_PRESENT_IN_COMPOSITION;

@Component
public class HealthIdValidator implements Validator<EncounterValidationContext> {

    private SHRProperties shrProperties;

    @Autowired
    public HealthIdValidator(SHRProperties shrProperties) {
        this.shrProperties = shrProperties;
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

            boolean subjectHasValue = hasValue(subject);
            if (resourceType.equals(ResourceType.Composition) && !subjectHasValue) {
                validationMessages.add(new ValidationMessage(ValidationMessage.Source.ProfileValidator, "invalid", "healthId",
                        HEALTH_ID_NOT_PRESENT_IN_COMPOSITION, OperationOutcome.IssueSeverity.error));
                return validationMessages;
            }

            if (!subjectHasValue) continue;

            ResourceReference subjectRef = (ResourceReference) subject.getValues().get(0);
            String healthIdFromUrl = validateAndIdentifyPatientId(subjectRef.getReferenceSimple(), expectedHealthId);
            if (healthIdFromUrl == null) {
                validationMessages.add(new ValidationMessage(ValidationMessage.Source.ProfileValidator, "invalid", atomEntry.getId(),
                        HEALTH_ID_NOT_MATCH, OperationOutcome.IssueSeverity.error));
            }
        }
        return validationMessages;
    }

    private String validateAndIdentifyPatientId(String patientUrl, String healthId) {
        String expectedUrl = shrProperties.getPatientReferencePath() + "/" + healthId;
        if (expectedUrl.trim().equalsIgnoreCase(patientUrl.trim())) {
            return getHealthIdFromUrl(patientUrl);
        }
        return null;
    }

    private boolean hasValue(Property subject) {
        return (subject != null) && subject.hasValues();

    }

    private String getHealthIdFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.length());
    }
}
