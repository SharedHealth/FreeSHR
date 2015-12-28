package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class DiagnosticReportPerformerIdentifierTest {

    private DiagnosticReportPerformerIdentifier diagnosticReportPerformerIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu2();
    private Bundle bundle;

    @Before
    public void setUp() {
        diagnosticReportPerformerIdentifier = new DiagnosticReportPerformerIdentifier();
        bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnosticReport_with_performer_reference.xml"), fhirContext);
    }

    @Test
    public void shouldValidateResourceOfTypeDiagnosticReport() {
        List<DiagnosticReport> reports = FhirResourceHelper.findBundleResourcesOfType(bundle, DiagnosticReport.class);
        assertTrue(diagnosticReportPerformerIdentifier.validates(reports.get(0)));
    }

    @Test
    public void shouldExtractProperDiagnosticReportPerformerReferences() {
        List<DiagnosticReport> diagnosticReports = FhirResourceHelper.findBundleResourcesOfType(bundle, DiagnosticReport.class);
        List<ResourceReferenceDt> providerReferences = diagnosticReportPerformerIdentifier.getProviderReferences(diagnosticReports.get(0));
        assertEquals("http://172.18.46.199:8080/api/1.0/providers/18.json", providerReferences.get(0).getReference().getValue());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        List<Composition> compositions = FhirResourceHelper.findBundleResourcesOfType(bundle, Composition.class);
        assertFalse(diagnosticReportPerformerIdentifier.validates(compositions.get(0)));
    }

}