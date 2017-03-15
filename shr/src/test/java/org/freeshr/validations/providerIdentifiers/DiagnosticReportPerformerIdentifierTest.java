package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class DiagnosticReportPerformerIdentifierTest {

    private DiagnosticReportPerformerIdentifier diagnosticReportPerformerIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu3();
    private Bundle bundle;

    @Before
    public void setUp() {
        diagnosticReportPerformerIdentifier = new DiagnosticReportPerformerIdentifier();
        bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnosticReport_with_performer_reference.xml"), fhirContext);
    }

    @Test
    public void shouldValidateResourceOfTypeDiagnosticReport() {
        List<DiagnosticReport> reports = FhirResourceHelper.findBundleResourcesOfType(bundle, DiagnosticReport.class);
        assertTrue(diagnosticReportPerformerIdentifier.canValidate(reports.get(0)));
    }

    @Test
    public void shouldExtractProperDiagnosticReportPerformerReferences() {
        List<DiagnosticReport> diagnosticReports = FhirResourceHelper.findBundleResourcesOfType(bundle, DiagnosticReport.class);
        List<Reference> providerReferences = diagnosticReportPerformerIdentifier.getProviderReferences(diagnosticReports.get(0));
        assertEquals("http://172.18.46.199:8080/api/1.0/providers/18.json", providerReferences.get(0).getReference());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        List<Composition> compositions = FhirResourceHelper.findBundleResourcesOfType(bundle, Composition.class);
        assertFalse(diagnosticReportPerformerIdentifier.canValidate(compositions.get(0)));
    }

}
