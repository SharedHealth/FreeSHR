package org.freeshr.validations.providerIdentifiers;


import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class ConditionAsserterIdentifierTest {

    private ConditionAsserterIdentifier conditionAsserterIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu3();

    @Before
    public void setUp() {
        conditionAsserterIdentifier = new ConditionAsserterIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeCondition() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnoses.xml"), fhirContext);
        List<Condition> conditions = FhirResourceHelper.findBundleResourcesOfType(bundle, Condition.class);
        assertTrue(conditionAsserterIdentifier.canValidate(conditions.get(0)));
    }

    @Test
    public void shouldExtractProperConditionAsserterReferences() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnoses.xml"), fhirContext);
        List<Condition> conditions = FhirResourceHelper.findBundleResourcesOfType(bundle, Condition.class);
        List<Reference> providerReferences = conditionAsserterIdentifier.getProviderReferences(conditions.get(0));
        assertEquals(1, providerReferences.size());
        assertEquals("http://localhost:9997/providers/19.json", providerReferences.get(0).getReference());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnoses.xml"), fhirContext);
        List<Encounter> encounters = FhirResourceHelper.findBundleResourcesOfType(bundle, Encounter.class);
        assertFalse(conditionAsserterIdentifier.canValidate(encounters.get(0)));
    }



}
