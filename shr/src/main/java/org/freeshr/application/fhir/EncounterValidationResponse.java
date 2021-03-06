package org.freeshr.application.fhir;

import org.freeshr.validations.FhirMessageFilter;
import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.utilities.validation.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

public class EncounterValidationResponse {

    private List<Error> errors = new ArrayList<>();
    private String encounterId;
    private Bundle bundle;

    public void addError(Error error) {
        errors.add(error);
    }

    public boolean isSuccessful() {
        return errors.isEmpty();
    }

    public boolean isNotSuccessful() {
        return !isSuccessful();
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public static EncounterValidationResponse fromValidationMessages(List<ValidationMessage> validationMessages,
                                                                     FhirMessageFilter filter) {
        return filter.filterMessagesSevereThan(validationMessages, OperationOutcome.IssueSeverity.WARNING);
    }

    public static EncounterValidationResponse fromShrValidationMessages(List<ShrValidationMessage> validationMessages) {
        return FhirMessageFilter.createResponse(validationMessages, Severity.ERROR);
    }


    public static EncounterValidationResponse createErrorResponse(Exception e) {
        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.addError(new Error("Unknown", "invalid", e.getMessage()));
        return encounterValidationResponse;
    }

    @Override
    public String toString() {
        return "EncounterValidationResponse{" +
                "errors=" + errors +
                ", encounterId='" + encounterId + '\'' +
                '}';
    }

    public void mergeErrors(EncounterValidationResponse aResponse) {
        for (Error error : aResponse.getErrors()) {
            addError(error);
        }
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
