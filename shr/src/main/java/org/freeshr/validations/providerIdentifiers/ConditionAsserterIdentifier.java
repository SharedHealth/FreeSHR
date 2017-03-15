package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ConditionAsserterIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof Condition);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        Condition condition = (Condition) resource;
        if (condition.getAsserter().isEmpty()){
            return Collections.emptyList();
        }
        return Arrays.asList(condition.getAsserter());
    }
}
