package org.freeshr.validations;


import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResourceValidator implements Validator<Bundle> {

    public static final String INVALID = "invalid";
    public static final String CODE_UNKNOWN = "code-unknown";

    private Map<ResourceType, Validator<Bundle.BundleEntryComponent>> resourceTypeValidatorMap = new HashMap<>();

    @Autowired
    public ResourceValidator(ConditionValidator conditionValidator,
                             MedicationPrescriptionValidator medicationPrescriptionValidator,
                             ImmunizationValidator immunizationValidator,
                             ProcedureValidator procedureValidator) {
        assignDefaultValidatorToAllResourceTypes();
//        resourceTypeValidatorMap.put(ResourceType.Condition, conditionValidator);
        //resourceTypeValidatorMap.put(ResourceType.MedicationOrder, medicationPrescriptionValidator);
        //resourceTypeValidatorMap.put(ResourceType.Immunization, immunizationValidator);
        //resourceTypeValidatorMap.put(ResourceType.Procedure, procedureValidator);

    }

    private void assignDefaultValidatorToAllResourceTypes() {
        for (ResourceType resourceType : ResourceType.values()) {
            resourceTypeValidatorMap.put(resourceType, new DefaultValidator());
        }
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<Bundle> subject) {
        Bundle feed = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();

        for (final Bundle.BundleEntryComponent atomEntry : feed.getEntry()) {
            Validator<Bundle.BundleEntryComponent> validator =
                    resourceTypeValidatorMap.get(atomEntry.getResource().getResourceType());
            validationMessages.addAll(validator.validate(atomEntryFragment(atomEntry)));
        }
        return validationMessages;
    }

    private ValidationSubject<Bundle.BundleEntryComponent>
    atomEntryFragment(final Bundle.BundleEntryComponent atomEntry) {
        return new ValidationSubject<Bundle.BundleEntryComponent>() {
            @Override
            public Bundle.BundleEntryComponent extract() {
                return atomEntry;
            }
        };
    }

}
