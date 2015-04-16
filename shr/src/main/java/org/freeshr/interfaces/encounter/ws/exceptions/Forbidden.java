package org.freeshr.interfaces.encounter.ws.exceptions;

import org.freeshr.application.fhir.EncounterResponse;

public class Forbidden extends RuntimeException {
    private String errorMessage;

    public Forbidden(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public Forbidden(EncounterResponse encounterResponse) {
        super();
        errorMessage = encounterResponse.getErrors().toString();
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
