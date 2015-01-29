package org.freeshr.validations;


import org.freeshr.domain.ErrorMessageBuilder;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.domain.ErrorMessageBuilder.buildValidationMessage;

@Component
public class ProcedureValidator implements Validator<AtomEntry<? extends Resource>> {

    private static final Logger logger = LoggerFactory.getLogger(ProcedureValidator.class);
    private static String DATE = "date";
    private static String REPORT = "report";
    private DateValidator dateValidator;

    @Autowired
    public ProcedureValidator(DateValidator dateValidator) {
        this.dateValidator = dateValidator;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomEntry<? extends Resource>> subject) {
        AtomEntry<? extends Resource> atomEntry = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();


        if (!validateDate(atomEntry, validationMessages)) {
            return validationMessages;

        }

        if (!validateDiagnosticReport(atomEntry, validationMessages)) {
            return validationMessages;

        }

        return validationMessages;
    }

    //TODO: Decide if this func should be written(checks if link is present in the entire feed (Now checking for whether the link is not empty)
    private boolean validateDiagnosticReport(AtomEntry<? extends Resource> atomEntry, List<ValidationMessage> validationMessages) {
        String id = atomEntry.getId();
        Property report = atomEntry.getResource().getChildByName(REPORT);
        List<Element> reportElements = report.getValues();
        for (Element reportElement : reportElements) {
            if (reportElement instanceof ResourceReference) {
                ResourceReference reference = (ResourceReference) reportElement;
                if (reference.getReferenceSimple() == null || reference.getReferenceSimple().isEmpty()) {
                    logger.error("Should have reference to Diagnostic Report resource");
                    validationMessages.add(buildValidationMessage(id, ResourceValidator.INVALID, ErrorMessageBuilder.INVALID_DIAGNOSTIC_REPORT_REFERNECE, OperationOutcome.IssueSeverity.error));
                    return false;
                }
            }
        }


        return true;

    }

    private boolean validateDate(AtomEntry<? extends Resource> atomEntry, List<ValidationMessage> validationMessages) {
        String id = atomEntry.getId();
        Property date = atomEntry.getResource().getChildByName(DATE);
        List<Element> dateElements = date.getValues();
        for (Element element : dateElements) {
            if (element instanceof Period) {
                Period period = (Period) element;
                DateAndTime endDate = period.getEndSimple();
                DateAndTime startDate = period.getStartSimple();


                if (!dateValidator.isValidPeriod(startDate, endDate)) {
                    logger.error("Invalid Period Date. ");
                    validationMessages.add(buildValidationMessage(id, ResourceValidator.INVALID, ErrorMessageBuilder.INVALID_PERIOD, OperationOutcome.IssueSeverity.error));
                    return false;
                }

            }
        }

        return true;
    }
}
