package org.freeshr.interfaces.encounter.ws.exceptions;


import org.freeshr.application.fhir.EncounterResponse;

public class PreconditionFailed extends RuntimeException {

    private EncounterResponse result;

    public PreconditionFailed(EncounterResponse result) {
        this.result = result;
    }

    public EncounterResponse getResult() {
        return result;
    }

    @Override
    public String getMessage() {
        return String.format("Precondition failed: %s", result.getErrors().toString());
    }
}
