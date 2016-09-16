package org.freeshr.validations.providerIdentifiers;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class ConditionAsserterIdentifierTest {

    private ConditionAsserterIdentifier conditionAsserterIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu2();

    @Before
    public void setUp() {
        conditionAsserterIdentifier = new ConditionAsserterIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeCondition() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml"), fhirContext);
        List<Condition> conditions = FhirResourceHelper.findBundleResourcesOfType(bundle, Condition.class);
        assertTrue(conditionAsserterIdentifier.validates(conditions.get(0)));
    }

    @Test
    public void shouldExtractProperConditionAsserterReferences() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml"), fhirContext);
        List<Condition> conditions = FhirResourceHelper.findBundleResourcesOfType(bundle, Condition.class);
        List<ResourceReferenceDt> providerReferences = conditionAsserterIdentifier.getProviderReferences(conditions.get(0));
        assertEquals(1, providerReferences.size());
        assertEquals("http://localhost:9997/providers/19.json", providerReferences.get(0).getReference().getValue());

//        references = conditionAsserterIdentifier.getProviderReferences(getResource("xmls/encounters/providers_identifiers/condition_no_asserter" +
//                ".xml", ResourceType.Condition));
//        assertNull(references);

    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml"), fhirContext);
        List<Encounter> encounters = FhirResourceHelper.findBundleResourcesOfType(bundle, Encounter.class);
        assertFalse(conditionAsserterIdentifier.validates(encounters.get(0)));
    }



}