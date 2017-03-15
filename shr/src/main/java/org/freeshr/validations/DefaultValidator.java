package org.freeshr.validations;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.utilities.validation.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

public class DefaultValidator implements Validator<Bundle.BundleEntryComponent> {
    @Override
    public List<ValidationMessage> validate(ValidationSubject<Bundle.BundleEntryComponent> subject) {
        return new ArrayList<>();
    }
}
