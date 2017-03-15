package org.freeshr.validations.resource;


import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.SubResourceValidator;
import org.freeshr.validations.ValidationMessages;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.freeshr.utils.DateUtil.isValidPeriod;

@Component
public class ProcedureValidator implements SubResourceValidator {

    private static final Logger logger = LoggerFactory.getLogger(ProcedureValidator.class);
    public static final String PROCEDURE_PERIOD_LOCATION = "f:Procedure/f:performed";


    @Override
    public boolean validates(Object resource) {
        return (resource instanceof Procedure);
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource) {
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        Procedure procedure = (Procedure) resource;
        validationMessages.addAll(validateProcedureDates(procedure));
        if (validationMessages.size() > 0) return validationMessages;

        validationMessages.addAll(validateDiagnosticReport(procedure.getReport()));

        return validationMessages;
    }

    private Collection<? extends ShrValidationMessage> validateDiagnosticReport(List<Reference> reports) {
        //TODO Shouldn't we be validating diagnosticReport separately
        List<ShrValidationMessage> validationMessages = new ArrayList<>();

        for (Reference report : reports) {
            if (report.isEmpty()) continue; //procedure can be without report
            if (StringUtils.isBlank(report.getReference())) {
                 validationMessages.add(new ShrValidationMessage(Severity.ERROR, "f:Procedure/f:report", "invalid", ValidationMessages
                         .INVALID_DIAGNOSTIC_REPORT_REFERENCE));
            }
        }
        return validationMessages;
    }

    private Collection<? extends ShrValidationMessage> validateProcedureDates(Procedure procedure) {
        IDatatype performed = (IDatatype) procedure.getPerformed();

        if (performed instanceof Period) {
            Period procedurePeriod = (Period) performed;
            if (!isValidPeriod(procedurePeriod.getStart(), procedurePeriod.getEnd())) {
                logger.error(String.format("Procedure:Encounter failed for %s", ValidationMessages.INVALID_PERIOD));
                return Arrays.asList(new ShrValidationMessage(Severity.ERROR, PROCEDURE_PERIOD_LOCATION,
                        "invalid", ValidationMessages.INVALID_PERIOD  + ":Procedure:" + procedure.getId() ));
            }
        } else if (performed instanceof DateTimeDt) {
            Date value = ((DateTimeDt) performed).getValue();
            if (value == null) {
                return Arrays.asList(new ShrValidationMessage(Severity.ERROR, PROCEDURE_PERIOD_LOCATION,
                        "invalid", "Invalid Procedure perform date:" + procedure.getId()));
            }
        }
        return new ArrayList<>();
    }
}
