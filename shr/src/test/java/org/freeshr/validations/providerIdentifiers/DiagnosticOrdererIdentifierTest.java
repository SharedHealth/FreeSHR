package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class DiagnosticOrdererIdentifierTest {

    private DiagnosticOrdererIdentifier diagnosticOrdererIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu2();

    @Before
    public void setUp() {
        diagnosticOrdererIdentifier = new DiagnosticOrdererIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeDiagnosticOrder() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnostic_order.xml"), fhirContext);
        List<DiagnosticOrder> orders = FhirResourceHelper.findBundleResourcesOfType(bundle, DiagnosticOrder.class);
        assertTrue(diagnosticOrdererIdentifier.validates(orders.get(0)));
    }

    @Test
    public void shouldExtractProperDiagnosticOrdererReference() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnostic_order.xml"), fhirContext);
        List<DiagnosticOrder> orders = FhirResourceHelper.findBundleResourcesOfType(bundle, DiagnosticOrder.class);
        List<ResourceReferenceDt> providerReferences = diagnosticOrdererIdentifier.getProviderReferences(orders.get(0));
        assertEquals(1, providerReferences.size());
        assertEquals("http://localhost:9997/providers/18.json", providerReferences.get(0).getReference().getValue());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnostic_order.xml"), fhirContext);
        List<Encounter> encounters = FhirResourceHelper.findBundleResourcesOfType(bundle, Encounter.class);
        assertFalse(diagnosticOrdererIdentifier.validates(encounters.get(0)));
    }

}