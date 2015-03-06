package org.freeshr.validations.providerIdentifiers;

import org.freeshr.utils.AtomFeedHelper;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ImmunizationProviderIdentifierTest {

    private ImmunizationProviderIdentifier immunizationProviderIdentifier;

    @Before
    public void setUp() {
        immunizationProviderIdentifier = new ImmunizationProviderIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeImmunization() {
        assertTrue(immunizationProviderIdentifier.validates(getResource("xmls/encounters/providers_identifiers/immunization.xml", ResourceType.Immunization)));
    }

    @Test
    public void shouldExtractProperImmunizationParticipantReferences() {
        List<String> references = immunizationProviderIdentifier.extractUrls(getResource("xmls/encounters/providers_identifiers/immunization.xml", ResourceType.Immunization));
        assertEquals(2, references.size());
        assertEquals("http://127.0.0.1:9997/providers/18.json", references.get(0));
        assertEquals("http://127.0.0.1:9997/providers/48.json", references.get(1));

        references = immunizationProviderIdentifier.extractUrls(getResource("xmls/encounters/providers_identifiers/immunization_with_performer_only.xml", ResourceType.Immunization));
        assertEquals(1, references.size());
        assertEquals("http://127.0.0.1:9997/providers/48.json", references.get(0));
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        assertFalse(immunizationProviderIdentifier.validates(getResource("xmls/encounters/providers_identifiers/encounter_with_valid_participant.xml", ResourceType.Encounter)));
    }

    private Resource getResource(String file, ResourceType resType) {
        ValidationSubject<AtomEntry<? extends Resource>> validationSubject = AtomFeedHelper.getAtomFeed(file, resType);
        return validationSubject.extract().getResource();
    }

}