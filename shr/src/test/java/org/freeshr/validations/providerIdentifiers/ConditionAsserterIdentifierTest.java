package org.freeshr.validations.providerIdentifiers;


import org.freeshr.utils.AtomFeedHelper;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConditionAsserterIdentifierTest {

    private ConditionAsserterIdentifier conditionAsserterIdentifier;

    @Before
    public void setUp() {
        conditionAsserterIdentifier = new ConditionAsserterIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeCondition() {
        assertTrue(conditionAsserterIdentifier.validates(getResource("xmls/encounters/providers_identifiers/condition.xml", ResourceType.Condition)));
    }

    @Test
    public void shouldExtractProperConditionAsserterReferences() {
        List<String> references = conditionAsserterIdentifier.extractUrls(getResource("xmls/encounters/providers_identifiers/condition.xml",
                ResourceType.Condition));
        assertEquals(1, references.size());
        assertEquals("http://127.0.0.1:9997/providers/18.json", references.get(0));

        references = conditionAsserterIdentifier.extractUrls(getResource("xmls/encounters/providers_identifiers/condition_no_asserter.xml", ResourceType.Condition));
        assertNull(references);

    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        assertFalse(conditionAsserterIdentifier.validates(getResource("xmls/encounters/providers_identifiers/encounter_with_valid_participant.xml",
                ResourceType.Encounter)));
    }

    private Resource getResource(String file, ResourceType resType) {
        ValidationSubject<AtomEntry<? extends Resource>> validationSubject = AtomFeedHelper.getAtomFeed(file, resType);
        return validationSubject.extract().getResource();
    }


}