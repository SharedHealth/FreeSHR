package org.freeshr.validations;

import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

public class DefaultValidator implements Validator<AtomEntry<? extends Resource>> {
    @Override
    public List<ValidationMessage> validate(EncounterValidationFragment<AtomEntry<? extends Resource>> fragment) {
        return new ArrayList<>();
    }
}
