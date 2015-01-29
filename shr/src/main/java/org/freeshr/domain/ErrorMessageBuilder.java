package org.freeshr.domain;

import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.validation.ValidationMessage;

public class ErrorMessageBuilder {
    public static final String INVALID_DOSAGE_QUANTITY = "Invalid Dosage Quantity";

    public static final String HEALTH_ID_NOT_PRESENT = "Patient's Health Id is not present.";
    public static final String HEALTH_ID_NOT_MATCH = "Patient's Health Id does not match.";
    public static final String HEALTH_ID_NOT_PRESENT_IN_COMPOSITION = "Composition must have patient's Health Id in subject.";

    public static final String INVALID_MEDICATION_REFERENCE_URL = "Invalid Medication Reference URL";
    public static final String UNSPECIFIED_MEDICATION = "Unspecified Medication";
    public static final String INVALID_DISPENSE_MEDICATION_REFERENCE_URL = "Invalid Dispense-Medication Reference URL";

    public static final String FEED_MUST_HAVE_COMPOSITION= "Feed must have a Composition with an encounter.";

    public static final String UNKNOWN_CONDITION_RELATION_CODE= "Unknown ConditionRelationshipType code";

    public static final String INVALID_PERIOD= "Invalid Period";
    public static final String INVALID_DATE= "Invalid Date";
    public static final String INVALID_DIAGNOSTIC_REPORT_REFERNECE= "Invalid Diagnostic Report Reference";

    public static ValidationMessage buildValidationMessage(String path, String type, String message, OperationOutcome.IssueSeverity level) {
        ValidationMessage validationMessage = new ValidationMessage();
        validationMessage.setSource(null);
        validationMessage.setLocation(path);
        validationMessage.setType(type);
        validationMessage.setMessage(message);
        validationMessage.setLevel(level);

        return validationMessage;
    }
}
