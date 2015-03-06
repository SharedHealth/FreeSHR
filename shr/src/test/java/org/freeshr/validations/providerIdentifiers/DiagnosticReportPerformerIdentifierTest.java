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

public class DiagnosticReportPerformerIdentifierTest {

    private DiagnosticReportPerformerIdentifier diagnosticReportPerformerIdentifier;

    @Before
    public void setUp() {
        diagnosticReportPerformerIdentifier = new DiagnosticReportPerformerIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeDiagnosticReport() {
        assertTrue(diagnosticReportPerformerIdentifier.validates(getResource("xmls/encounters/providers_identifiers/diagnostic_report.xml", ResourceType.DiagnosticReport)));
    }

    @Test
    public void shouldExtractProperDiagnosticReportPerformerReferences() {
        List<String> references = diagnosticReportPerformerIdentifier.extractUrls(getResource("xmls/encounters/providers_identifiers/diagnostic_report.xml", ResourceType.DiagnosticReport));
        assertEquals(1, references.size());
        assertEquals("http://127.0.0.1:9997/providers/18.json", references.get(0));

        references = diagnosticReportPerformerIdentifier.extractUrls(getResource("xmls/encounters/providers_identifiers/diagnostic_report_no_performer.xml", ResourceType.DiagnosticReport));
        assertNull(references);
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        assertFalse(diagnosticReportPerformerIdentifier.validates(getResource("xmls/encounters/providers_identifiers/encounter_with_valid_participant.xml", ResourceType.Encounter)));
    }

    private Resource getResource(String file, ResourceType resType) {
        ValidationSubject<AtomEntry<? extends Resource>> validationSubject = AtomFeedHelper.getAtomFeed(file, resType);
        return validationSubject.extract().getResource();
    }

}