package org.freeshr.infrastructure.security;

import org.freeshr.config.SHRProperties;
import org.freeshr.events.EncounterEvent;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FhirResourceHelper;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.exceptions.FHIRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConfidentialEncounterHandler {
    private FhirFeedUtil fhirFeedUtil;
    private final static Logger logger = LoggerFactory.getLogger(ConfidentialEncounterHandler.class);

    @Autowired
    public ConfidentialEncounterHandler(FhirFeedUtil fhirFeedUtil, SHRProperties shrProperties) {
        this.fhirFeedUtil = fhirFeedUtil;
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
        String encounterContent;
        encounterContent = replacedContentWithDstu2(encounterEvent);
        encounterEvent.getEncounterBundle().setEncounterContent(encounterContent);
    }

    private String replacedContentWithDstu2(EncounterEvent encounterEvent) {
        Bundle originalBundle = fhirFeedUtil.parseBundle(encounterEvent.getContent(), "xml");

        Composition originalComposition = FhirResourceHelper.findBundleResourcesOfType(originalBundle, Composition.class).get(0);
        Composition composition = new Composition();
        composition.setSubject(originalComposition.getSubject());
        Composition.DocumentConfidentiality value = null;
        try {
            value = Composition.DocumentConfidentiality.fromCode(encounterEvent.getConfidentialityLevel().getLevel());
        } catch (FHIRException e) {
            logger.error("Cannot determine confidentiality for %s", encounterEvent.getConfidentialityLevel().getLevel());
        }
        composition.setConfidentiality(value);
        composition.setStatus(originalComposition.getStatus());
        composition.setDate(originalComposition.getDate());
        composition.setAuthor(originalComposition.getAuthor());
        composition.setType(originalComposition.getType());
        composition.setId(originalComposition.getId());
        composition.setIdentifier(originalComposition.getIdentifier());

        Bundle bundle = new Bundle();
        bundle.setType(originalBundle.getType());
        Bundle.BundleEntryComponent entry = bundle.addEntry();
        Bundle.BundleEntryComponent compositionEntry = FhirResourceHelper.getBundleEntriesForResource(originalBundle, "Composition").get(0);
        entry.setFullUrl(compositionEntry.getFullUrl());
        entry.setResource(composition);

        return fhirFeedUtil.encodeBundle(bundle, "xml");
    }
}
