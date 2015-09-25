package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.utils.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EncounterParticipantIdentifier extends ClinicalResourceProviderIdentifier {

    @Override
    protected boolean validates(IResource resource) {
        return (resource instanceof Encounter);
    }

    @Override
    protected List<ResourceReferenceDt> getProviderReferences(IResource resource) {
        List<Encounter.Participant> participants = ((Encounter) resource).getParticipant();
        List<ResourceReferenceDt> participantRefs = new ArrayList<>();
        for (Encounter.Participant participant : participants) {
            participantRefs.add(participant.getIndividual());
        }
        return participantRefs;
    }
}
