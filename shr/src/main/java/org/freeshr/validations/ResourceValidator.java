package org.freeshr.validations;


import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResourceValidator {

    public static final String INVALID = "invalid";

    private ResourceOrFeedDeserializer resourceOrFeedDeserializer;
    private Map<ResourceType, Validator> resourceTypeValidatorMap = new HashMap<ResourceType, Validator>();

    public ResourceValidator() {
        this.resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
        populateValidatorMap();
    }

    private void populateValidatorMap() {
        assignDefaultValidatorToAllResourceTypes();
        resourceTypeValidatorMap.put(ResourceType.Condition, new ConditionValidator());
    }

    private void assignDefaultValidatorToAllResourceTypes() {
        for (ResourceType resourceType : ResourceType.values()) {
            resourceTypeValidatorMap.put(resourceType, new DefaultValidator());
        }
    }

    public List<ValidationMessage> validate(String sourceXml) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(sourceXml);
        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            ResourceType resourceType = atomEntry.getResource().getResourceType();
            Validator validator = resourceTypeValidatorMap.get(resourceType);
            validator.validate(validationMessages, atomEntry);
        }
        return validationMessages;
    }
}
