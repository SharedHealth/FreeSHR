package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DiagnosticOrdererIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean validates(IResource resource) {
        return (resource instanceof DiagnosticOrder);
    }

    @Override
    protected List<ResourceReferenceDt> getProviderReferences(IResource resource) {
        return Arrays.asList(((DiagnosticOrder) resource).getOrderer());
    }
}


