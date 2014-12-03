package org.freeshr.validations;


import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResourceValidator {

    public static final String INVALID = "invalid";
    public static final String CODE_UNKNOWN = "code-unknown";

    private Map<ResourceType, Validator> resourceTypeValidatorMap = new HashMap<>();

    public ResourceValidator() {
        assignDefaultValidatorToAllResourceTypes();
        resourceTypeValidatorMap.put(ResourceType.Condition, new ConditionValidator());
    }

    private void assignDefaultValidatorToAllResourceTypes() {
        for (ResourceType resourceType : ResourceType.values()) {
            resourceTypeValidatorMap.put(resourceType, new DefaultValidator());
        }
    }

    public List<ValidationMessage> validate(AtomFeed feed) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            ResourceType resourceType = atomEntry.getResource().getResourceType();
            Validator validator = resourceTypeValidatorMap.get(resourceType);
            validator.validate(validationMessages, atomEntry);
        }
        return validationMessages;
    }
}
