package org.freeshr.application.fhir;

import org.freeshr.infrastructure.tr.TerminologyServer;
import org.hl7.fhir.instance.model.Code;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.EMPTY_LIST;
import static org.hl7.fhir.instance.model.ValueSet.ValueSetDefineConceptComponent;
import static org.hl7.fhir.instance.model.ValueSet.ValueSetExpansionContainsComponent;

@Component
public class TRConceptLocator implements ConceptLocator {

    private TerminologyServer terminologyServer;

    private final static Logger logger = LoggerFactory.getLogger(TRConceptLocator.class);

    @Autowired
    public TRConceptLocator(TerminologyServer terminologyServer) {
        this.terminologyServer = terminologyServer;
    }

    @Override
    public ValueSetDefineConceptComponent locate(String system, final String code) {
        try {
            final Boolean isValid = terminologyServer.isValid(system, code).toBlocking().first();
            if (isValid) {
                Code conceptCode = new Code();
                conceptCode.setValue(code);
                return new ValueSetDefineConceptComponent(conceptCode);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.warn("Problem while validating concept", e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "trCache", unless = "#result != null")
    public ValidationResult validate(String system, String code, String display) {
        if (locate(system, code) == null) {
            return new ValidationResult(OperationOutcome.IssueSeverity.error, display);
        }
        return null;
    }

    @Override
    public boolean verifiesSystem(String system) {
        //return StringUtils.contains(system, "openmrs");
        return terminologyServer.verifiesSystem(system);
    }

    @Override
    public List<ValueSetExpansionContainsComponent> expand(ValueSet.ConceptSetComponent inc) throws Exception {
        return EMPTY_LIST;
    }
}
