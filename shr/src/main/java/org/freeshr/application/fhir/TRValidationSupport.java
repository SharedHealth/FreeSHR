package org.freeshr.application.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.IValidationSupport;
import org.freeshr.infrastructure.tr.TerminologyServer;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TRValidationSupport implements IValidationSupport {

    private final TerminologyServer terminologyServer;

    @Autowired
    public TRValidationSupport(TerminologyServer terminologyServer) {
        this.terminologyServer = terminologyServer;
    }

    @Override
    public ValueSet.ValueSetExpansionComponent expandValueSet(ValueSet.ConceptSetComponent theInclude) {
        return null;
    }

    @Override
    public ValueSet fetchCodeSystem(String theSystem) {
        return null;
    }

    @Override
    public <T extends IBaseResource> T fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {
        return null;
    }

    @Override
    public boolean isCodeSystemSupported(String theSystem) {
        return terminologyServer.verifiesSystem(theSystem);
    }

    @Override
    public CodeValidationResult validateCode(String theCodeSystem, String theCode, String theDisplay) {
        return null;
    }
}
