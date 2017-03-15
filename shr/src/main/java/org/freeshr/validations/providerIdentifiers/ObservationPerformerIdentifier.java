package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObservationPerformerIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof Observation);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        return ((Observation) resource).getPerformer();
    }
}
