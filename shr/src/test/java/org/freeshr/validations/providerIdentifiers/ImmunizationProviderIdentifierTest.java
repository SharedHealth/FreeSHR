package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class ImmunizationProviderIdentifierTest {

    private ImmunizationProviderIdentifier immunizationProviderIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu2();
    private ca.uhn.fhir.model.dstu2.resource.Bundle bundle;

    @Before
    public void setUp() {
        immunizationProviderIdentifier = new ImmunizationProviderIdentifier();
        bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_immunization.xml"), fhirContext);
    }

    @Test
    public void shouldValidateResourceOfTypeImmunization() {
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);
        assertTrue(immunizationProviderIdentifier.validates(immunizations.get(0)));
    }

    @Test
    public void shouldExtractProperImmunizationParticipantReferences() {
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);

        List<ResourceReferenceDt> references = immunizationProviderIdentifier.getProviderReferences(immunizations.get(0));
        assertEquals(2, references.size());
        assertEquals("http://172.18.46.199:8080/api/1.0/providers/18.json", references.get(0).getReference().getValue());
        assertEquals("http://172.18.46.199:8080/api/1.0/providers/19.json", references.get(1).getReference().getValue());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        assertFalse(immunizationProviderIdentifier.validates(new Encounter()));
    }

}