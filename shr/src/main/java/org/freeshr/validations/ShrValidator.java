package org.freeshr.validations;


import ca.uhn.fhir.validation.SingleValidationMessage;

import java.util.List;

public interface ShrValidator<T> {
    List<ShrValidationMessage> validate(ValidationSubject<T> subject);
}
