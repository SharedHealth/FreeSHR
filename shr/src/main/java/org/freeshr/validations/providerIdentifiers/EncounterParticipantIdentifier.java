package org.freeshr.validations.providerIdentifiers;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EncounterParticipantIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean canValidate(Resource resource) {
        return (resource instanceof Encounter);
    }

    @Override
    protected List<Reference> getProviderReferences(Resource resource) {
        List<Encounter.EncounterParticipantComponent> participants = ((Encounter) resource).getParticipant();
        List<Reference> participantRefs = new ArrayList<>();
        for (Encounter.EncounterParticipantComponent participant : participants) {
            participantRefs.add(participant.getIndividual());
        }
        return participantRefs;
    }
}
