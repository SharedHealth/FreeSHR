package org.freeshr.application.fhir;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.freeshr.validations.FhirMessageFilter;
import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.validation.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

public class EncounterValidationResponse {

    private List<Error> errors = new ArrayList<>();
    private String encounterId;
    private org.hl7.fhir.instance.model.Bundle feed;
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
        return filter.filterMessagesSevereThan(validationMessages,
                OperationOutcome.IssueSeverity.WARNING);
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

    public void setFeed(org.hl7.fhir.instance.model.Bundle feed) {
        this.feed = feed;
    }

    public org.hl7.fhir.instance.model.Bundle getFeed() {
        return feed;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
