package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

@Component
public class ProcedureRequesterIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof ProcedureRequest);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        ProcedureRequest procedureRequest = (ProcedureRequest) resource;
        if (procedureRequest.getRequester().isEmpty()){
            return Collections.emptyList();
        }
        return asList(procedureRequest.getRequester().getAgent());
    }
}


