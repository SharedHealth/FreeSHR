package org.freeshr.validations;

import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

public class DefaultValidator extends AtomEntryValidator {

    @Override
    public List<ValidationMessage> validate(AtomEntry<? extends Resource> atomEntry) {
        return new ArrayList<>();
    }
}
