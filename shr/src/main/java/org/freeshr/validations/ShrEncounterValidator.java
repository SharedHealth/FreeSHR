package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterValidationResponse;

public interface ShrEncounterValidator {
    EncounterValidationResponse validate(EncounterValidationContext validationContext);
    String supportedVersion();
}
