package org.freeshr.validations;

import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

public class DefaultValidator implements Validator<Bundle.BundleEntryComponent> {
    @Override
    public List<ValidationMessage> validate(ValidationSubject<Bundle.BundleEntryComponent> subject) {
        return new ArrayList<>();
    }
}
