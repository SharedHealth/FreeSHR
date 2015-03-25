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

public class ObservationPerformerIdentifierTest {

    private ObservationPerformerIdentifier observationPerformerIdentifier;

    @Before
    public void setUp() {
        observationPerformerIdentifier = new ObservationPerformerIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeObservation() {
        assertTrue(observationPerformerIdentifier.validates(getResource("xmls/encounters/providers_identifiers/observation.xml",
                ResourceType.Observation)));
    }

    @Test
    public void shouldExtractProperObservationPerformerReferences() {
        List<String> references = observationPerformerIdentifier.extractUrls(getResource
                ("xmls/encounters/providers_identifiers/observation.xml", ResourceType.Observation));
        assertEquals(1, references.size());
        assertEquals("http://127.0.0.1:9997/providers/18.json", references.get(0));
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        assertFalse(observationPerformerIdentifier.validates(getResource
                ("xmls/encounters/providers_identifiers/encounter_with_valid_participant.xml", ResourceType.Encounter)));
    }

    private Resource getResource(String file, ResourceType resType) {
        ValidationSubject<AtomEntry<? extends Resource>> validationSubject = AtomFeedHelper.getAtomFeed(file, resType);
        return validationSubject.extract().getResource();
    }

}