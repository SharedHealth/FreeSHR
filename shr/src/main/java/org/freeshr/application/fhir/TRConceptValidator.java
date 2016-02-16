package org.freeshr.application.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.IValidationSupport;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.config.SHRProperties;
import org.freeshr.infrastructure.tr.TerminologyServer;
import org.hl7.fhir.instance.model.Enumerations;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.ValueSet.ValueSetExpansionComponent;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;


@Component
public class TRConceptValidator implements IValidationSupport {

    private final TerminologyServer terminologyServer;
    private SHRProperties shrProperties;
    private final static Logger logger = LoggerFactory.getLogger(TRConceptValidator.class);
    private final HashMap<String, String> map;

    @Autowired
    public TRConceptValidator(TerminologyServer terminologyServer, SHRProperties shrProperties) {
        this.terminologyServer = terminologyServer;
        this.shrProperties = shrProperties;
        map = new HashMap<>();
        loadValueSetUrls();
    }

    private void loadValueSetUrls() {
        map.put("http://hl7.org/fhir/ValueSet/v3-FamilyMember", shrProperties.getTRLocationPath() + "/openmrs/ws/rest/v1/tr/vs/Relationship-Type");
    }

    @Override
    public ValueSetExpansionComponent expandValueSet(FhirContext theContext, ValueSet.ConceptSetComponent theInclude) {
        return null;
    }

    @Override
    public ValueSet fetchCodeSystem(FhirContext theContext, String theSystem) {
        return null;
    }

    @Override
    @Cacheable(value = "shrProfileCache", unless = "#result == null")
    public <T extends IBaseResource> T fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {
        IBaseResource baseResource = null;
        if (map.containsKey(theUri)) {
            String theSystem = map.get(theUri);
            ValueSet valueSet = new ValueSet();
            valueSet.setUrl(theUri);
            valueSet.setStatus(Enumerations.ConformanceResourceStatus.DRAFT);
            ValueSet.ValueSetComposeComponent valueSetComposeComponent = new ValueSet.ValueSetComposeComponent();
            valueSetComposeComponent.addInclude().setSystem(theSystem);
            valueSet.setCompose(valueSetComposeComponent);
            baseResource = valueSet;
        }
        return (T) baseResource;
    }


    @Override
    public boolean isCodeSystemSupported(FhirContext theContext, String theSystem) {
        return terminologyServer.verifiesSystem(StringUtils.trim(theSystem));
    }

    @Override
    @Cacheable(value = "trCache", unless = "#result.ok == false")
    public CodeValidationResult validateCode(FhirContext theContext, String theCodeSystem, String theCode, String theDisplay) {
        /** TODO - Note: should we be creating a custom CodeValidationResult and return that?
         * the caching expression "unless" uses the javabeans convention for boolean property for ok (isOk) rather than a field
         */
        CodeValidationResult result = locate(theCodeSystem, theCode, theDisplay);
        return result;
    }

    private CodeValidationResult locate(String system, String code, String display) {
        try {
            Boolean result = terminologyServer.isValid(system, code).toBlocking().first();
            if (result != null && result.booleanValue()) {
                ValueSet.ConceptDefinitionComponent def = new ValueSet.ConceptDefinitionComponent();
                def.setDefinition(system);
                def.setCode(code);
                def.setDisplay(display);
                return new CodeValidationResult(def);
            } else {
                return new CodeValidationResult(OperationOutcome.IssueSeverity.ERROR,
                        String.format("Could not validate concept system[%s], code[%s]", system, code));
            }
        } catch (Exception e) {
            logger.error(String.format("Problem while validating concept system[%s], code[%s]", system, code), e);
            return new CodeValidationResult(OperationOutcome.IssueSeverity.ERROR, "Couldn't identify system and code. error:" + e.getMessage());
        }
    }
}
