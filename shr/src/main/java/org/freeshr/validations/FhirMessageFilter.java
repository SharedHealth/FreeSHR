package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.Error;
import org.freeshr.utils.CollectionUtils;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FhirMessageFilter {

    private ArrayList<String> ignoreList;

    public FhirMessageFilter() {
        ignoreList = new ArrayList<>();
        ignoreList.add("f:Composition/f:Composition/f:type");
        ignoreList.add("f:DiagnosticOrder/f:item");
        ignoreList.add("f:DiagnosticReport/f:name");
    }

    public EncounterValidationResponse filterMessagesSevereThan(List<ValidationMessage> outputs,
                                                                final OperationOutcome.IssueSeverity severity) {
        return CollectionUtils.reduce(CollectionUtils.filter(outputs, new CollectionUtils.Fn<ValidationMessage,
                Boolean>() {
            @Override
            public Boolean call(ValidationMessage input) {
                //For SHR: We treat FHIR warning level as error.
                boolean possibleError = severity.compareTo(input.getLevel()) >= 0;
                // TODO :  remove the following if condition once the validation mechanism is finalised for
                // DiagnosticOrder
                if (possibleError) {
                    if (shouldFilterMessagesOfType(input)) {
                        possibleError = false;
                    }
                }
                return possibleError;

            }
        }), new EncounterValidationResponse(), new CollectionUtils.ReduceFn<ValidationMessage,
                EncounterValidationResponse>() {
            @Override
            public EncounterValidationResponse call(ValidationMessage input, EncounterValidationResponse acc) {
                org.freeshr.application.fhir.Error error = new Error();
                error.setField(input.getLocation());
                error.setType(input.getType().getDisplay());
                error.setReason(input.getMessage());
                acc.addError(error);
                return acc;
            }
        });
    }

    private boolean shouldFilterMessagesOfType(ValidationMessage input) {
        if (input.getLevel().equals(OperationOutcome.IssueSeverity.ERROR))
            return false;
        if (input.getType().toCode().equalsIgnoreCase("unknown")) {
            for (String ignoreString : ignoreList) {
                if (input.getLocation().contains(ignoreString)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static EncounterValidationResponse createResponse(List<ShrValidationMessage> outputs,
                                                      final Severity severity) {
        return CollectionUtils.reduce(CollectionUtils.filter(outputs,
                new CollectionUtils.Fn<ShrValidationMessage, Boolean>() {
            @Override
            public Boolean call(ShrValidationMessage input) {
                //For SHR: We treat FHIR warning level as error?
                boolean possibleError = severity.compareTo(input.getSeverity()) >= 0;
                // TODO :  remove the following if condition once the validation mechanism is finalised for
                // DiagnosticOrder
                if (possibleError) {
                    if (shouldFilterMessagesOfType(input)) {
                        possibleError = false;
                    }
                }
                return possibleError;

            }
        }), new EncounterValidationResponse(), new CollectionUtils.ReduceFn<ShrValidationMessage,
                EncounterValidationResponse>() {
            @Override
            public EncounterValidationResponse call(ShrValidationMessage input, EncounterValidationResponse acc) {
                Error error = new Error();
                error.setField(input.getLocation());
                error.setType(input.getType());
                error.setReason(input.getMessage());
                acc.addError(error);
                return acc;
            }
        });
    }

    public static boolean shouldFilterMessagesOfType(ShrValidationMessage input) {
        if (input.getSeverity().equals(Severity.ERROR))
            return false;
        else
            return false;
    }
}