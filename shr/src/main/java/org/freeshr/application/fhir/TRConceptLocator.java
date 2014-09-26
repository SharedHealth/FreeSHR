package org.freeshr.application.fhir;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.freeshr.infrastructure.tr.TerminologyServer;
import org.hl7.fhir.instance.model.Code;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TRConceptLocator implements ConceptLocator {

    private TerminologyServer terminologyServer;

    private static Logger logger = Logger.getLogger(TRConceptLocator.class);

    @Autowired
    public TRConceptLocator(TerminologyServer terminologyServer) {
        this.terminologyServer = terminologyServer;
    }

    @Override
    public ValueSet.ValueSetDefineConceptComponent locate(String system, final String code) {
        try {
            Boolean isValid = terminologyServer.isValid(system, code).get();
            if (isValid) {
                Code conceptCode = new Code();
                conceptCode.setValue(code);
                return new ValueSet.ValueSetDefineConceptComponent(conceptCode);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.warn(e);
            return null;
        }
    }

    @Override
    public ValidationResult validate(String system, String code, String display) {
        if (locate(system, code) == null) {
            return new ValidationResult(OperationOutcome.IssueSeverity.error, display);
        }
        return null;
    }

    @Override
    public boolean verifiesSystem(String system) {
        return StringUtils.contains(system, "openmrs");
    }

    @Override
    public List<ValueSet.ValueSetExpansionContainsComponent> expand(ValueSet.ConceptSetComponent inc) throws Exception {
        return Collections.EMPTY_LIST;
    }
}
