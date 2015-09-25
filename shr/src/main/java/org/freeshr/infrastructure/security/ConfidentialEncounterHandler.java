package org.freeshr.infrastructure.security;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRProperties;
import org.freeshr.events.EncounterEvent;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.instance.model.*;
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
        org.hl7.fhir.instance.model.Bundle originalFeed = fhirFeedUtil.deserialize(encounterEvent.getContent());

        org.hl7.fhir.instance.model.Bundle.BundleEntryComponent originalCompositionEntry = fhirFeedUtil.getAtomEntryOfResourceType(originalFeed.getEntry(), ResourceType.Composition);
        Composition originalComposition = (Composition) originalCompositionEntry.getResource();
        Composition composition = new Composition();
        composition.setSubject(originalComposition.getSubject());
        Coding confidentiality = new Coding();
        confidentiality.setCode(encounterEvent.getConfidentialityLevel().getLevel());
        composition.setConfidentiality(confidentiality.getCode());
        composition.setStatus(originalComposition.getStatus());
        composition.setDate(originalComposition.getDate());
        for (Reference resourceReference : originalComposition.getAuthor()) {
            Reference authorReference = composition.addAuthor();
            authorReference.setReference(resourceReference.getReference());

        }
        composition.setType(originalComposition.getType());

        org.hl7.fhir.instance.model.Bundle feed = new org.hl7.fhir.instance.model.Bundle();
        org.hl7.fhir.instance.model.Bundle.BundleEntryComponent atomEntry = new org.hl7.fhir.instance.model.Bundle.BundleEntryComponent();
        atomEntry.setResource(composition);
        atomEntry.setId(originalCompositionEntry.getId());
        atomEntry.setFullUrl("urn:uuid:" + originalCompositionEntry.getId());
        feed.getEntry().add(atomEntry);

        return fhirFeedUtil.serialize(feed);
    }
}
