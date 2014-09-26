package org.freeshr.application.fhir;

import org.freeshr.utils.CollectionUtils;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FhirMessageFilter {
    public FhirMessageFilter() {
    }

    public EncounterValidationResponse filterMessagesSevereThan(List<ValidationMessage> outputs, final OperationOutcome.IssueSeverity severity) {
        return CollectionUtils.reduce(CollectionUtils.filter(outputs, new CollectionUtils.Fn<ValidationMessage, Boolean>() {
            @Override
            public Boolean call(ValidationMessage input) {
                //For SHR: We treat FHIR warning level as error.
                boolean possibleError = severity.compareTo(input.getLevel()) >= 0;
                // TODO :  remove the following if condition once the validation mechanism is finalised for DiagnosticOrder
                if (possibleError) {
                    if (input.getType().equalsIgnoreCase("code-unknown") && input.getLocation().contains("f:DiagnosticOrder/f:item")) {
                        possibleError = false;
                    }
                }
                return possibleError;

            }
        }), new EncounterValidationResponse(), new CollectionUtils.ReduceFn<ValidationMessage, EncounterValidationResponse>() {
            @Override
            public EncounterValidationResponse call(ValidationMessage input, EncounterValidationResponse acc) {
                Error error = new Error();
                error.setField(input.getLocation());
                error.setType(input.getType());
                error.setReason(input.getMessage());
                acc.addError(error);
                return acc;
            }
        });
    }
}