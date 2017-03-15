package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncounterParticipantIdentifierTest {

    private EncounterParticipantIdentifier encounterParticipantIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu3();
    private Bundle bundle;

    @Before
    public void setUp() {
        encounterParticipantIdentifier = new EncounterParticipantIdentifier();
        bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnosticReport_with_performer_reference.xml"), fhirContext);
    }

    @Test
    public void shouldValidateResourceOfTypeEncounter() {
        List<Encounter> encounters = FhirResourceHelper.findBundleResourcesOfType(bundle, Encounter.class);
        assertTrue(encounterParticipantIdentifier.canValidate(encounters.get(0)));
    }

    @Test
    public void shouldExtractProperEncounterParticipantReferences() {
        List<Encounter> encounters = FhirResourceHelper.findBundleResourcesOfType(bundle, Encounter.class);
        List<Reference> providerReferences = encounterParticipantIdentifier.getProviderReferences(encounters.get(0));
        assertFalse(CollectionUtils.isEmpty(providerReferences));
        assertEquals("http://172.18.46.199:8080/api/1.0/providers/18.json", providerReferences.get(0).getReference());

    }

}
