package org.freeshr.infrastructure.security;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import org.freeshr.domain.model.Requester;
import org.freeshr.events.EncounterEvent;
import org.freeshr.interfaces.encounter.ws.APIIntegrationTestBase;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FhirResourceHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.assertEquals;

public class ConfidentialEncounterHandlerIT extends APIIntegrationTestBase {

    private ConfidentialEncounterHandler confidentialEncounterHandler;
    private FhirFeedUtil fhirFeedUtil;

    @Before
    public void setUp() throws Exception {
        fhirFeedUtil = new FhirFeedUtil();
        confidentialEncounterHandler = new ConfidentialEncounterHandler(fhirFeedUtil, shrProperties);
    }

    @Test
    public void shouldReplaceConfidentialEncounters() throws Exception {
        final Requester createdBy = new Requester("facilityId", "providerId");
        Date receivedAt = new Date();
        String confidentialContent = asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml");
        EncounterEvent confidentialEncounterEvent = new EncounterEvent(receivedAt, createEncounterBundle("encounter id", "98001046534",
                Confidentiality.Restricted, Confidentiality.Normal, confidentialContent,
                createdBy, receivedAt));
        List<EncounterEvent> replacedEncounterEvents = confidentialEncounterHandler.replaceConfidentialEncounterEvents(asList(confidentialEncounterEvent));
        assertEquals(1, replacedEncounterEvents.size());
        EncounterEvent replacedEncounterEvent = replacedEncounterEvents.get(0);
        assertEquals(Confidentiality.Restricted, replacedEncounterEvent.getConfidentialityLevel());
        String replacedContent = replacedEncounterEvent.getEncounterBundle().getContent();

        Bundle confidentialBundle = fhirFeedUtil.parseBundle(confidentialContent, "xml");
        Bundle replacedBundle = fhirFeedUtil.parseBundle(replacedContent, "xml");

        Composition originalComposition = FhirResourceHelper.findBundleResourcesOfType(confidentialBundle, Composition.class).get(0);
        Composition replacedComposition = FhirResourceHelper.findBundleResourcesOfType(replacedBundle, Composition.class).get(0);

        assertEquals(originalComposition.getStatus(), replacedComposition.getStatus());
        assertEquals(originalComposition.getDate(), replacedComposition.getDate());
        assertEquals(originalComposition.getAuthor().get(0).getReference(), replacedComposition.getAuthor().get(0).getReference());
        CodingDt replaceCompositionType = replacedComposition.getType().getCoding().get(0);
        CodingDt originalCompositionType = originalComposition.getType().getCoding().get(0);
        assertEquals(originalCompositionType.getCode(), replaceCompositionType.getCode());
        assertEquals(originalCompositionType.getSystem(), replaceCompositionType.getSystem());
        assertEquals(originalCompositionType.getDisplay(), replaceCompositionType.getDisplay());
        assertEquals(Confidentiality.Restricted.getLevel(), replacedComposition.getConfidentiality());

        assertEquals(1, replacedBundle.getEntry().size());
        assertEquals("Composition", replacedBundle.getEntry().get(0).getResource().getResourceName());
        Bundle.Entry replacedCompositionEntry = getCompositionEntry(replacedBundle);
        Bundle.Entry confidentialCompositionEntry = getCompositionEntry(confidentialBundle);
        assertEquals(confidentialCompositionEntry.getId(), replacedCompositionEntry.getId());
        assertEquals(confidentialCompositionEntry.getFullUrl(), replacedCompositionEntry.getFullUrl());
        //TODO verify something
//        assertEquals(confidentialCompositionEntry.getAuthorName(), replacedCompositionEntry.getAuthorName());
//        assertEquals(confidentialCompositionEntry.getAuthorUri(), replacedCompositionEntry.getAuthorUri());
    }

    private Composition getComposition(Bundle fhirFeed) {
        return (Composition) getCompositionEntry(fhirFeed).getResource();
    }

    private Bundle.Entry getCompositionEntry(Bundle bundle) {
        List<Bundle.Entry> entries = FhirResourceHelper.getBundleEntriesForResource(bundle, "Composition");
        return entries.get(0);
    }
}