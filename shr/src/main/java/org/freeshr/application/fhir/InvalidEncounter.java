package org.freeshr.application.fhir;

public class InvalidEncounter extends RuntimeException {

    private Error error;

    public InvalidEncounter(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public static final InvalidEncounter invalidDiagnosis(String code) {
        return new InvalidEncounter(new Error("511", "Invalid diagnosis: " + code));
    }

    public static final InvalidEncounter systemError() {
        return new InvalidEncounter(new Error("512", "System error while validating diagnosis"));
    }

    public static final InvalidEncounter missingSystem(String code) {
        return new InvalidEncounter(new Error("514", "referencing system is mandatory for " + code));
    }

    public static final InvalidEncounter invalidSystemUri() {
        return new InvalidEncounter(new Error("513", "The referencing system path for diagnosis is not valid"));
    }

}
