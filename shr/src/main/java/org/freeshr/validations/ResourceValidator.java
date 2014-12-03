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
public class ResourceValidator implements Validator<AtomFeed>{

    public static final String INVALID = "invalid";
    public static final String CODE_UNKNOWN = "code-unknown";

    private Map<ResourceType, AtomEntryValidator> resourceTypeValidatorMap = new HashMap<>();

    public ResourceValidator() {
        assignDefaultValidatorToAllResourceTypes();
        resourceTypeValidatorMap.put(ResourceType.Condition, new ConditionValidator());
    }

    private void assignDefaultValidatorToAllResourceTypes() {
        for (ResourceType resourceType : ResourceType.values()) {
            resourceTypeValidatorMap.put(resourceType, new DefaultValidator());
        }
    }

    @Override
    public List<ValidationMessage> validate(AtomFeed feed) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            AtomEntryValidator validator = resourceTypeValidatorMap.get(atomEntry.getResource().getResourceType());
            validationMessages.addAll(validator.validate(atomEntry));
        }
        return validationMessages;
    }
}
