package org.freeshr.application.fhir;

import java.util.ArrayList;
import java.util.List;

public class EncounterValidationResponse {

    private List<Error> errors = new ArrayList<>();
    private String encounterId;

    public void addError(Error error) {
        errors.add(error);
    }

    public boolean isSuccessful() {
        return errors.isEmpty();
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public EncounterValidationResponse addError(String field, String message) {
        Error error = new Error();
        error.setField(field);
        error.setReason(message);
        return this;
    }

    @Override
    public String toString() {
        return "EncounterValidationResponse{" +
                "errors=" + errors +
                ", encounterId='" + encounterId + '\'' +
                '}';
    }
}
