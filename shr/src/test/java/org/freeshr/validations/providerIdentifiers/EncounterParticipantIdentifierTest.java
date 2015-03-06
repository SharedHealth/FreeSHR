package org.freeshr.validations.providerIdentifiers;

import org.freeshr.utils.AtomFeedHelper;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.junit.Assert.*;

public class EncounterParticipantIdentifierTest {

    private EncounterParticipantIdentifier encounterParticipantIdentifier;

    @Before
    public void setUp() {
        encounterParticipantIdentifier = new EncounterParticipantIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeEncounter() {
        ValidationSubject<AtomEntry<? extends Resource>> validationSubject = AtomFeedHelper.getAtomFeed("xmls/encounters/providers_identifiers/encounter_with_valid_participant.xml",
                ResourceType.Encounter);
        assertTrue(encounterParticipantIdentifier.validates(validationSubject.extract().getResource()));
    }

    @Test
    public void shouldExtractProperEncounterParticipantReferences() {
        ValidationSubject<AtomEntry<? extends Resource>> validationSubject = AtomFeedHelper.getAtomFeed("xmls/encounters/providers_identifiers/encounter_with_valid_participant.xml",
                ResourceType.Encounter);
        List<String> references = encounterParticipantIdentifier.extractUrls(validationSubject.extract().getResource());
        assertTrue(!CollectionUtils.isEmpty(references));
        assertEquals("http://127.0.0.1:9997/providers/18.json", references.get(0));

    }

}