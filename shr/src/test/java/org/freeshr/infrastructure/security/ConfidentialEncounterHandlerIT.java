package org.freeshr.infrastructure.security;

import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Requester;
import org.freeshr.events.EncounterEvent;
import org.freeshr.interfaces.encounter.ws.APIIntegrationTestBase;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.assertEquals;

public class ConfidentialEncounterHandlerIT extends APIIntegrationTestBase {
    private static final String VALID_HEALTH_ID = "5893922485019082753";

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
        String confidentialContent = asString("xmls/encounters/encounter_restricted_with_normal_patient.xml");
        EncounterEvent confidentialEncounterEvent = new EncounterEvent(receivedAt, createEncounterBundle("encounter id", VALID_HEALTH_ID,
                Confidentiality.Restricted, Confidentiality.Normal, confidentialContent,
                createdBy, receivedAt));
        List<EncounterEvent> replacedEncounterEvents = confidentialEncounterHandler.replaceConfidentialEncounterEvents(asList(confidentialEncounterEvent));
        assertEquals(1, replacedEncounterEvents.size());
        EncounterEvent replacedEncounterEvent = replacedEncounterEvents.get(0);
        assertEquals(Confidentiality.Restricted, replacedEncounterEvent.getConfidentialityLevel());
        String replacedContent = replacedEncounterEvent.getEncounterBundle().getContent();

        Bundle confidentialFeed = fhirFeedUtil.deserialize(confidentialContent);
        Bundle replacedFeed = fhirFeedUtil.deserialize(replacedContent);

        Composition originalComposition = getComposition(confidentialFeed);
        Composition replacedComposition = getComposition(replacedFeed);

        assertEquals(originalComposition.getStatus().getDefinition(), replacedComposition.getStatus().getDefinition());
        assertEquals(originalComposition.getDate().toString(), replacedComposition.getDate().toString());
        assertEquals(originalComposition.getAuthor().get(0).getReference(), replacedComposition.getAuthor().get(0).getReference());
        Coding replaceCompositionType = replacedComposition.getType().getCoding().get(0);
        Coding originalCompositionType = originalComposition.getType().getCoding().get(0);
        assertEquals(originalCompositionType.getCode(), replaceCompositionType.getCode());
        assertEquals(originalCompositionType.getSystem(), replaceCompositionType.getSystem());
        assertEquals(originalCompositionType.getDisplay(), replaceCompositionType.getDisplay());
        assertEquals(Confidentiality.Restricted.getLevel(), replacedComposition.getConfidentiality());

        assertEquals(1, replacedFeed.getEntry().size());
        assertEquals(ResourceType.Composition, replacedFeed.getEntry().get(0).getResource().getResourceType());
        Bundle.BundleEntryComponent replacedCompositionEntry = getCompositionEntry(replacedFeed);
        Bundle.BundleEntryComponent confidentialCompositionEntry = getCompositionEntry(confidentialFeed);
        assertEquals(confidentialCompositionEntry.getId(), replacedCompositionEntry.getId());
        //TODO
//        assertEquals(confidentialCompositionEntry.getAuthorName(), replacedCompositionEntry.getAuthorName());
//        assertEquals(confidentialCompositionEntry.getAuthorUri(), replacedCompositionEntry.getAuthorUri());
    }

    private Composition getComposition(Bundle fhirFeed) {
        return (Composition) getCompositionEntry(fhirFeed).getResource();
    }

    private Bundle.BundleEntryComponent getCompositionEntry(Bundle fhirFeed) {
        return fhirFeedUtil.getAtomEntryOfResourceType(fhirFeed.getEntry(), ResourceType.Composition);
    }
}