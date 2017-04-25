package org.freeshr.validations.resource;


import org.apache.commons.lang3.StringUtils;
import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.SubResourceValidator;
import org.freeshr.validations.ValidationMessages;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.freeshr.utils.DateUtil.isValidPeriod;

@Component
public class ProcedureValidator implements SubResourceValidator {

    private static final Logger logger = LoggerFactory.getLogger(ProcedureValidator.class);
    public static final String PROCEDURE_PERIOD_LOCATION = "Bundle.entry[%s].resource.performed";
    public static final String PROCEDURE_REPORT_LOCATION = "Bundle.entry[%s].resource.report";


    @Override
    public boolean validates(Object resource) {
        return (resource instanceof Procedure);
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource, int entryIndex) {
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        Procedure procedure = (Procedure) resource;
        validationMessages.addAll(validateProcedureDates(procedure, entryIndex));
        if (validationMessages.size() > 0) return validationMessages;

        validationMessages.addAll(validateDiagnosticReport(procedure.getReport(), entryIndex));

        return validationMessages;
    }

    private Collection<? extends ShrValidationMessage> validateDiagnosticReport(List<Reference> reports, int entryIndex) {
        //TODO Shouldn't we be validating diagnosticReport separately
        List<ShrValidationMessage> validationMessages = new ArrayList<>();

        for (Reference report : reports) {
            if (report.isEmpty()) continue; //procedure can be without report
            if (StringUtils.isBlank(report.getReference())) {
                String location = String.format(PROCEDURE_REPORT_LOCATION, entryIndex);
                validationMessages.add(new ShrValidationMessage(Severity.ERROR, location, "invalid", ValidationMessages
                        .INVALID_DIAGNOSTIC_REPORT_REFERENCE));
            }
        }
        return validationMessages;
    }

    private Collection<? extends ShrValidationMessage> validateProcedureDates(Procedure procedure, int entryIndex) {
        Type performed = procedure.getPerformed();

        String location = String.format(PROCEDURE_PERIOD_LOCATION, entryIndex);
        if (performed instanceof Period) {
            Period procedurePeriod = (Period) performed;
            if (!isValidPeriod(procedurePeriod.getStart(), procedurePeriod.getEnd())) {
                logger.error(String.format("Procedure:Encounter failed for %s", ValidationMessages.INVALID_PERIOD));
                return Arrays.asList(new ShrValidationMessage(Severity.ERROR, location,
                        "invalid", ValidationMessages.INVALID_PERIOD + ":Procedure:" + procedure.getId()));
            }
        } else if (performed instanceof DateTimeType) {
            Date value = ((DateTimeType) performed).getValue();
            if (value == null) {
                return Arrays.asList(new ShrValidationMessage(Severity.ERROR, location,
                        "invalid", "Invalid Procedure perform date:" + procedure.getId()));
            }
        }
        return new ArrayList<>();
    }
}
