package org.freeshr.infrastructure.security;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRProperties;
import org.freeshr.events.EncounterEvent;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConfidentialEncounterHandler {
    private FhirFeedUtil fhirFeedUtil;
    private SHRProperties shrProperties;

    @Autowired
    public ConfidentialEncounterHandler(FhirFeedUtil fhirFeedUtil, SHRProperties shrProperties) {
        this.fhirFeedUtil = fhirFeedUtil;
        this.shrProperties = shrProperties;
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
        String encounterContent = "";
        if ("v1".equals(shrProperties.getFhirDocumentSchemaVersion())) {
            encounterContent = replacedContentWithDstu1(encounterEvent);
        } else {
            encounterContent = replacedContentWithDstu2(encounterEvent);
        }
        encounterEvent.getEncounterBundle().setEncounterContent(encounterContent);
    }

    private String replacedContentWithDstu2(EncounterEvent encounterEvent) {
        Bundle originalBundle = fhirFeedUtil.parseBundle(encounterEvent.getContent(), "xml");

        ca.uhn.fhir.model.dstu2.resource.Composition originalComposition = originalBundle.getAllPopulatedChildElementsOfType(ca.uhn.fhir.model.dstu2.resource.Composition.class).get(0);
        ca.uhn.fhir.model.dstu2.resource.Composition composition = new ca.uhn.fhir.model.dstu2.resource.Composition();
        composition.setSubject(originalComposition.getSubject());
        composition.setConfidentiality(originalComposition.getConfidentiality());
        composition.setStatus(originalComposition.getStatusElement());
        composition.setDate(originalComposition.getDateElement());
        composition.setAuthor(originalComposition.getAuthor());
        composition.setType(originalComposition.getType());
        composition.setId(originalComposition.getId());
        composition.setIdentifier(originalComposition.getIdentifier());

        Bundle bundle = new Bundle();
        bundle.setType(originalBundle.getTypeElement());
        Bundle.Entry entry = bundle.addEntry();
        entry.setResource(composition);

        return fhirFeedUtil.encodeBundle(bundle, "xml");
    }

    private String replacedContentWithDstu1(EncounterEvent encounterEvent) {
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

        return fhirFeedUtil.serialize(feed);
    }
}
