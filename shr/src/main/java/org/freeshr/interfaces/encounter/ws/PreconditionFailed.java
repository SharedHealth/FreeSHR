package org.freeshr.interfaces.encounter.ws;


import org.freeshr.application.fhir.EncounterResponse;

public class PreconditionFailed extends RuntimeException {

    private EncounterResponse result;

    public PreconditionFailed(EncounterResponse result) {
        this.result = result;
    }

    public EncounterResponse getResult() {
        return result;
    }
}
