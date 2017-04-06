package org.freeshr.validations;

import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.List;

public interface Validator<T> {
    List<ValidationMessage> validate(ValidationSubject<T> subject);
}
