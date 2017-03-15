package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcedurePerformerIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof Procedure);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        List<Procedure.ProcedurePerformerComponent> performers = ((Procedure) resource).getPerformer();
        List<Reference> references = new ArrayList<>();
        for (Procedure.ProcedurePerformerComponent performer : performers) {
            references.add(performer.getActor());
        }
        return references;
    }
}



