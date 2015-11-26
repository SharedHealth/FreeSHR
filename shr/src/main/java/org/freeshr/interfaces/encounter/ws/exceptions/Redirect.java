package org.freeshr.interfaces.encounter.ws.exceptions;

import org.freeshr.application.fhir.EncounterResponse;

public class Redirect extends RuntimeException {
    private String errorMessage;

    public Redirect(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public Redirect(EncounterResponse encounterResponse) {
        super();
        this.errorMessage = encounterResponse.getErrors().toString();
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
