package org.freeshr.validations;

import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.List;

public abstract class Validator {

    public static final String CODEABLE_CONCEPT = "CodeableConcept";

    public abstract void validate(List<ValidationMessage> validationMessages, AtomEntry<? extends Resource> atomEntry);

    abstract boolean skipCheckForThisTypeOfEntry(AtomEntry<? extends Resource> atomEntry);

    public void checkCodeableConcept(List<ValidationMessage> validationMessages, Property property, AtomEntry<? extends Resource> atomEntry) {
        if (!property.getTypeCode().equals(CODEABLE_CONCEPT)) return;

        if (!property.hasValues()) return;

        boolean bothSystemAndCodePresent = bothSystemAndCodePresent(property);
        if (bothSystemAndCodePresent || skipCheckForThisTypeOfEntry(atomEntry)) {
            return;
        }
        ValidationMessage validationMessage = new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(), String.format("'%s' is non-coded in the Condition", property.getName()), OperationOutcome.IssueSeverity.error);
        validationMessages.add(validationMessage);
    }

    private boolean bothSystemAndCodePresent(Property property) {
        boolean bothSystemAndCodePresent = false;
        List<Coding> codings = ((CodeableConcept) property.getValues().get(0)).getCoding();
        for (Coding coding : codings) {
            bothSystemAndCodePresent |= (coding.getSystem() != null && coding.getCode() != null);
        }
        return bothSystemAndCodePresent;
    }

}
