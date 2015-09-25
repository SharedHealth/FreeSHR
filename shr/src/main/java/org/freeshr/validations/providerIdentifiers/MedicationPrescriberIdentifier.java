package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MedicationPrescriberIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean validates(IResource resource) {
        return (resource instanceof MedicationOrder);
    }

    @Override
    protected List<ResourceReferenceDt> getProviderReferences(IResource resource) {
        return Arrays.asList(((MedicationOrder) resource).getPrescriber() );
    }
}



