package org.freeshr.application.fhir;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.freeshr.interfaces.encounter.ws.exceptions.PreconditionFailed;
import org.freeshr.interfaces.encounter.ws.exceptions.Redirect;
import org.freeshr.interfaces.encounter.ws.exceptions.UnProcessableEntity;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class EncounterResponse {

    private TypeOfFailure typeOfFailure = TypeOfFailure.None;
    private String encounterId;
    private List<Error> errors;

    public EncounterResponse() {
        this.typeOfFailure = TypeOfFailure.None;
        this.errors = Collections.emptyList();
    }

    public boolean isSuccessful() {
        return isTypeOfFailure(TypeOfFailure.None);
    }

    public List<Error> getErrors() {
        return errors;
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
        this.errors = encounterValidationResponse.getErrors();
        this.typeOfFailure = TypeOfFailure.Validation;
        return this;
    }

    @JsonIgnore
    public EncounterResponse preconditionFailure(String field, String type, String message) {
        this.errors = asList(new Error(field, type, message));
        this.typeOfFailure = TypeOfFailure.Precondition;
        return this;
    }

    @JsonIgnore
    public EncounterResponse forbidden(String field, String type, String message) {
        this.errors = asList(new Error(field, type, message));
        this.typeOfFailure = TypeOfFailure.Forbidden;
        return this;
    }

    @JsonIgnore
    public EncounterResponse activePatientFailure(String field, String type, String message) {
        this.errors = asList(new Error(field, type, message));
        this.typeOfFailure = TypeOfFailure.InactivePatient;
        return this;
    }

    public Exception getErrorResult() {
        if (isTypeOfFailure(TypeOfFailure.Precondition)) {
            return new PreconditionFailed(this);
        } else if (isTypeOfFailure(TypeOfFailure.Validation)) {
            return new UnProcessableEntity(this);
        } else if (isTypeOfFailure(TypeOfFailure.Forbidden)) {
            return new Forbidden(this);
        } else if (isTypeOfFailure(TypeOfFailure.InactivePatient))
            return new Redirect(this);

        return null;
    }

    public static enum TypeOfFailure {
        Validation,
        Precondition,
        Forbidden,
        InactivePatient,
        None
    }

}
