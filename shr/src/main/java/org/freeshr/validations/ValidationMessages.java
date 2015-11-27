package org.freeshr.validations;

public class ValidationMessages {

    public static final String INVALID_DOSAGE_QUANTITY = "Invalid Dosage Quantity";
    public static final String HEALTH_ID_NOT_MATCH = "Patient's Health Id does not match.";
    public static final String HEALTH_ID_NOT_PRESENT_IN_COMPOSITION = "Composition must have patient's Health Id in " +
            "subject.";
    public static final String INVALID_MEDICATION_REFERENCE_URL = "Invalid Medication Reference URL";
    public static final String UNSPECIFIED_MEDICATION = "Unspecified Medication";
    public static final String INVALID_DISPENSE_MEDICATION_REFERENCE_URL = "Invalid Dispense-Medication Reference URL";
    public static final String FEED_MUST_HAVE_COMPOSITION = "Feed must have a Composition with an encounter.";
    public static final String INVALID_PERIOD = "Invalid Period";
    public static final String INVALID_DIAGNOSTIC_REPORT_REFERENCE = "Invalid Diagnostic Report Reference";
    public static final String INVALID_PROVIDER_URL = "Invalid Provider URL";
    public static final String INVALID_AUTHOR = "Author must be a valid HIE Facility";
    public static final String INVALID_PATIENT = "Patient not available in patient registry";
    public static final String INACTIVE_PATIENT_MSG_PATTERN = "%s has been moved and replaced with %s";
    public static final String ENCOUNTER_NOT_FOUND_MSG_PATTERN = "Encounter (%s) not available.";

}
