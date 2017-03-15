package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class MedicationPrescriberIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof MedicationRequest);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        MedicationRequest medicationRequest = (MedicationRequest) resource;
        if (medicationRequest.getRequester().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(medicationRequest.getRequester().getAgent());
    }
}



