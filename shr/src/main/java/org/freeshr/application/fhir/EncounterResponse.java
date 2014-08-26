package org.freeshr.application.fhir;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EncounterResponse {

    private EncounterValidationResponse encounterValidationResponse;
    private TypeOfFailure typeOfFailure = TypeOfFailure.None;
    private String encounterId;
    private Error preconditionFailure;

    public boolean isSuccessful() {
        return isTypeOfFailure(TypeOfFailure.None);
    }

    public List<Error> getErrors() {
        if (isTypeOfFailure(TypeOfFailure.Validation)) {
            return encounterValidationResponse.getErrors();
        } else if (isTypeOfFailure(TypeOfFailure.Precondition)) {
            return Arrays.asList(preconditionFailure);
        } else {
            return Collections.emptyList();
        }
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    @JsonIgnore
    public boolean isTypeOfFailure(TypeOfFailure expectedTypeOfFailure) {
        return this.typeOfFailure.equals(expectedTypeOfFailure);
    }

    @JsonIgnore
    public EncounterResponse setValidationFailure(EncounterValidationResponse encounterValidationResponse) {
        this.encounterValidationResponse = encounterValidationResponse;
        this.typeOfFailure = TypeOfFailure.Validation;
        return this;
    }

    @JsonIgnore
    public EncounterResponse preconditionFailure(String field, String message) {
        this.preconditionFailure = new Error(field, message);
        this.typeOfFailure = TypeOfFailure.Precondition;
        return this;
    }

    public static enum TypeOfFailure {
        Validation,
        Precondition,
        None
    }
}
