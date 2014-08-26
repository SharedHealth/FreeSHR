package org.freeshr.interfaces.encounter.ws;


import org.freeshr.application.fhir.EncounterResponse;

import java.util.List;

public class UnProcessableEntity extends RuntimeException {

    private EncounterResponse result;

    public UnProcessableEntity(EncounterResponse result) {
        this.result = result;
    }

    public List<org.freeshr.application.fhir.Error> getErrors() {
        return result.getErrors();
    }

    public boolean isSuccessful() {
        return result.isSuccessful();
    }

    public String getEncounterId() {
        return result.getEncounterId();
    }
}
