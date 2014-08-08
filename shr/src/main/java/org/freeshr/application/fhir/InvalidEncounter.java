package org.freeshr.application.fhir;

public class InvalidEncounter extends RuntimeException {

    public static final InvalidEncounter INVALID_DIAGNOSIS = new InvalidEncounter(new Error("511", "Invalid diagnosis"));
    public static final InvalidEncounter SYSTEM_ERROR = new InvalidEncounter(new Error("512", "System error while validating diagnosis"));
    public static final InvalidEncounter INVALID_SYSTEM_URI = new InvalidEncounter(new Error("513", "The system path for diagnosis is not valid"));
    public static final InvalidEncounter DIAGNOSIS_SHOULD_HAVE_SYSTEM = new InvalidEncounter(new Error("514", "system is mandatory for diagnosis"));


    private Error error;

    public InvalidEncounter(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public static final InvalidEncounter invalidDiagnosis(String code){
        return new InvalidEncounter(new Error("511", "Invalid diagnosis: " + code));
    }
}
