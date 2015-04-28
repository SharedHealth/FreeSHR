package org.freeshr.validations;


import org.freeshr.utils.DateUtil;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.utils.DateUtil.isValidPeriod;

@Component
public class ProcedureValidator implements Validator<AtomEntry<? extends Resource>> {

    private static final Logger logger = LoggerFactory.getLogger(ProcedureValidator.class);

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomEntry<? extends Resource>> subject) {
        AtomEntry<? extends Resource> atomEntry = subject.extract();
        List<ValidationMessage> validationMessages = new ArrayList<>();

        validationMessages.addAll(validateDate(atomEntry));
        if (validationMessages.size() > 0) return validationMessages;

        validationMessages.addAll(validateDiagnosticReport(atomEntry));
        return validationMessages;
    }

    private List<ValidationMessage> validateDiagnosticReport(AtomEntry<? extends Resource> atomEntry) {
        Property report = atomEntry.getResource().getChildByName("report");
        List<Element> reportElements = report.getValues();
        for (Element reportElement : reportElements) {
            if (!(reportElement instanceof ResourceReference)) continue;
            ResourceReference reference = (ResourceReference) reportElement;
            if (reference.getReferenceSimple() == null || reference.getReferenceSimple().isEmpty()) {
                logger.debug(String.format("Procedure:Encounter failed for %s", ValidationMessages.INVALID_DIAGNOSTIC_REPORT_REFERENCE));
                return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(), ValidationMessages
                        .INVALID_DIAGNOSTIC_REPORT_REFERENCE, OperationOutcome.IssueSeverity.error));
            }
        }
        return new ArrayList<>();

    }

    private List<ValidationMessage> validateDate(AtomEntry<? extends Resource> atomEntry) {
        Property date = atomEntry.getResource().getChildByName("date");
        for (Element element : date.getValues()) {
            if (!(element instanceof Period)) continue;

            Period period = (Period) element;
            if (!isValidPeriod(period.getStartSimple(), period.getEndSimple())) {
                logger.debug(String.format("Procedure:Encounter failed for %s", ValidationMessages.INVALID_PERIOD));
                return validationMessages(new ValidationMessage(null, ResourceValidator.INVALID, atomEntry.getId(), ValidationMessages
                        .INVALID_PERIOD, OperationOutcome.IssueSeverity.error));
            }
        }

        return new ArrayList<>();
    }

    private List<ValidationMessage> validationMessages(ValidationMessage message) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        validationMessages.add(message);
        return validationMessages;
    }
}
