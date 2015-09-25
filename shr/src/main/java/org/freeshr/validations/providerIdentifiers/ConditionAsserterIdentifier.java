package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ConditionAsserterIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean validates(IResource resource) {
        return (resource instanceof Condition);
    }

    @Override
    protected List<ResourceReferenceDt> getProviderReferences(IResource resource) {
        return Arrays.asList(((Condition) resource).getAsserter());
    }
}
