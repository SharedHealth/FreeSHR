package org.freeshr.application.fhir;

public class InvalidEncounter extends RuntimeException {

    public static final InvalidEncounter INVALID_DIAGNOSIS = new InvalidEncounter(new Error("511", "Invalid diagnosis"));

    private Error error;

    public InvalidEncounter(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
