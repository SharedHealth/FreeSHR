package org.freeshr.infrastructure.security;

import org.freeshr.domain.model.Requester;
import org.freeshr.events.EncounterEvent;
import org.freeshr.interfaces.encounter.ws.APIIntegrationTestBase;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.assertEquals;

public class ConfidentialEncounterHandlerIT extends APIIntegrationTestBase {
    private static final String VALID_HEALTH_ID = "5893922485019082753";

    private ConfidentialEncounterHandler confidentialEncounterHandler = new ConfidentialEncounterHandler();
    private FhirFeedUtil fhirFeedUtil = new FhirFeedUtil();

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

        AtomFeed confidentialFeed = fhirFeedUtil.deserialize(confidentialContent);
        AtomFeed replacedFeed = fhirFeedUtil.deserialize(replacedContent);

        Composition originalComposition = getComposition(confidentialFeed);
        Composition replacedComposition = getComposition(replacedFeed);

        assertEquals(originalComposition.getStatus().getValue(), replacedComposition.getStatus().getValue());
        assertEquals(originalComposition.getDateSimple().toString(), replacedComposition.getDateSimple().toString());
        assertEquals(originalComposition.getAuthor().get(0).getReferenceSimple(), replacedComposition.getAuthor().get(0).getReferenceSimple());
        Coding replaceCompositionType = replacedComposition.getType().getCoding().get(0);
        Coding originalCompositionType = originalComposition.getType().getCoding().get(0);
        assertEquals(originalCompositionType.getCodeSimple(), replaceCompositionType.getCodeSimple());
        assertEquals(originalCompositionType.getSystemSimple(), replaceCompositionType.getSystemSimple());
        assertEquals(originalCompositionType.getDisplaySimple(), replaceCompositionType.getDisplaySimple());
        assertEquals(Confidentiality.Restricted.getLevel(), replacedComposition.getConfidentiality().getCodeSimple());

        assertEquals(1, replacedFeed.getEntryList().size());
        assertEquals(ResourceType.Composition, replacedFeed.getEntryList().get(0).getResource().getResourceType());
        AtomEntry replacedCompositionEntry = getCompositionEntry(replacedFeed);
        AtomEntry confidentialCompositionEntry = getCompositionEntry(confidentialFeed);
        assertEquals(confidentialCompositionEntry.getId(), replacedCompositionEntry.getId());
        assertEquals(confidentialCompositionEntry.getAuthorName(), replacedCompositionEntry.getAuthorName());
        assertEquals(confidentialCompositionEntry.getAuthorUri(), replacedCompositionEntry.getAuthorUri());
    }

    private Composition getComposition(AtomFeed fhirFeed) {
        return (Composition) getCompositionEntry(fhirFeed).getResource();
    }

    private AtomEntry getCompositionEntry(AtomFeed fhirFeed) {
        return fhirFeedUtil.getAtomEntryOfResourceType(fhirFeed.getEntryList(), ResourceType.Composition);
    }
}