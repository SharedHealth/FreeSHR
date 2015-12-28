package org.freeshr.util;

import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.Error;
import org.freeshr.validations.ShrValidationMessage;

import java.util.List;

import static org.junit.Assert.*;

public class ValidationFailureTestHelper {

    @Deprecated
    public static void assertFailureFromResponseErrors(String fieldName, String reason, List<Error> errors) {
        for (Error error : errors) {
            if (error.getReason().equals(reason)) {
                assertEquals(reason, error.getReason());
                return;
            }
        }
        fail(String.format("Couldn't find expected error with fieldName [%s] reason [%s]", fieldName, reason));
    }

    public static void assertFailureFromShrValidationMessages(String location, String message, List<ShrValidationMessage> shrValidationMessages) {
        for (ShrValidationMessage shrValidationMessage : shrValidationMessages) {
            if (shrValidationMessage.getLocation().equals(location)) {
                assertEquals(message, shrValidationMessage.getMessage());
                return;
            }
        }
        fail(String.format("Couldn't find expected error with location [%s] message [%s]", location, message));
    }

    public static void assertFailureInResponse(String field, String message, boolean partialSearch, EncounterValidationResponse response) {
        for (Error error : response.getErrors()) {
            if (error.getField().equals(field)) {
                boolean result = partialSearch ? error.getReason().startsWith(message) : error.getReason().equals(message);
                if (result) return;
            }
        }
        fail("Unable to find expected validation error with matching field and message:");
    }
}
