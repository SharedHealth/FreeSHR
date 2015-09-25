package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ProcedurePerformerIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean validates(IResource resource) {
        return (resource instanceof Procedure);
    }

    @Override
    protected List<ResourceReferenceDt> getProviderReferences(IResource resource) {
        List<Procedure.Performer> performers = ((Procedure) resource).getPerformer();
        List<ResourceReferenceDt> references = new ArrayList<>();
        for (Procedure.Performer performer : performers) {
            references.add(performer.getActor());
        }
        return references;
    }
}



