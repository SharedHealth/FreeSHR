package org.freeshr.validations;

import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.List;

public class DefaultValidator extends Validator {

    @Override
    public void validate(List<ValidationMessage> validationMessages, AtomEntry<? extends Resource> atomEntry) {

    }

    @Override
    boolean skipCheckForThisTypeOfEntry(AtomEntry<? extends Resource> atomEntry) {
        return false;
    }
}
