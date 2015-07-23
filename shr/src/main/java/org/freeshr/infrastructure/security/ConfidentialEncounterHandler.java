package org.freeshr.infrastructure.security;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.events.EncounterEvent;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.List;

public class ConfidentialEncounterHandler {
    private FhirFeedUtil fhirFeedUtil;

    public ConfidentialEncounterHandler() {
        fhirFeedUtil = new FhirFeedUtil();
    }

    public List<EncounterEvent> replaceConfidentialEncounterEvents(List<EncounterEvent> encounterEvents) {
        List<EncounterEvent> encounterEventsForFeed = new ArrayList<>();
        for (EncounterEvent encounterEvent : encounterEvents) {
            if (encounterEvent.isConfidential()) {
                replaceEncounterBundle(encounterEvent);
            }
            encounterEventsForFeed.add(encounterEvent);
        }
        return encounterEventsForFeed;
    }

    private void replaceEncounterBundle(EncounterEvent encounterEvent) {
        AtomFeed originalFeed = fhirFeedUtil.deserialize(encounterEvent.getContent());

        AtomEntry<? extends Resource> originalCompositionEntry = fhirFeedUtil.getAtomEntryOfResourceType(originalFeed.getEntryList(), ResourceType.Composition);
        Composition originalComposition = (Composition) originalCompositionEntry.getResource();
        Composition composition = new Composition();
        composition.setSubject(originalComposition.getSubject());
        Coding confidentiality = new Coding();
        confidentiality.setCodeSimple(encounterEvent.getConfidentialityLevel().getLevel());
        composition.setConfidentiality(confidentiality);
        composition.setStatus(originalComposition.getStatus());
        composition.setDate(originalComposition.getDate());
        for (ResourceReference resourceReference : originalComposition.getAuthor()) {
            ResourceReference authorReference = composition.addAuthor();
            authorReference.setReferenceSimple(resourceReference.getReferenceSimple());
        }
        composition.setType(originalComposition.getType());

        AtomFeed feed = new AtomFeed();
        AtomEntry atomEntry = new AtomEntry();
        atomEntry.setResource(composition);
        atomEntry.setId(originalCompositionEntry.getId());
        atomEntry.setAuthorName(originalCompositionEntry.getAuthorName());
        atomEntry.setAuthorUri(originalCompositionEntry.getAuthorUri());
        feed.getEntryList().add(atomEntry);

        EncounterBundle encounterBundle = encounterEvent.getEncounterBundle();
        encounterBundle.setEncounterContent(fhirFeedUtil.serialize(feed));
    }
}
