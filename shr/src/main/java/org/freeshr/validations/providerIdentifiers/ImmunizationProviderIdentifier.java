package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ImmunizationProviderIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean validates(IResource resource) {
        return (resource instanceof Immunization);
    }

    @Override
    protected List<ResourceReferenceDt> getProviderReferences(IResource resource) {
        Immunization immunization = (Immunization) resource;
        return Arrays.asList(immunization.getRequester(), immunization.getPerformer());
    }
}
