package org.freeshr.interfaces.encounter.ws.exceptions;


import org.freeshr.application.fhir.EncounterResponse;

public class UnProcessableEntity extends RuntimeException {

    private EncounterResponse result;

    public UnProcessableEntity(EncounterResponse result) {
        this.result = result;
    }

    public EncounterResponse getResult() {
        return result;
    }

    @Override
    public String getMessage() {
        return String.format("Unprocessable entity : %s", result.getErrors().toString());
    }
}
