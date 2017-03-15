package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class ImmunizationProviderIdentifierTest {

    private ImmunizationProviderIdentifier immunizationProviderIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu3();
    private Bundle bundle;

    @Before
    public void setUp() {
        immunizationProviderIdentifier = new ImmunizationProviderIdentifier();
        bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_immunization.xml"), fhirContext);
    }

    @Test
    public void shouldValidateResourceOfTypeImmunization() {
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);
        assertTrue(immunizationProviderIdentifier.canValidate(immunizations.get(0)));
    }

    @Test
    public void shouldExtractProperImmunizationParticipantReferences() {
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);

        List<Reference> references = immunizationProviderIdentifier.getProviderReferences(immunizations.get(0));
        assertEquals(2, references.size());
        assertEquals("http://172.18.46.199:8080/api/1.0/providers/19.json", references.get(0).getReference());
        assertEquals("http://172.18.46.199:8080/api/1.0/providers/18.json", references.get(1).getReference());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        assertFalse(immunizationProviderIdentifier.canValidate(new Encounter()));
    }

}
