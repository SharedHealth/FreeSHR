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

public class DiagnosticOrdererIdentifierTest {

    private DiagnosticOrdererIdentifier diagnosticOrdererIdentifier;

    @Before
    public void setUp() {
        diagnosticOrdererIdentifier = new DiagnosticOrdererIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeDiagnosticOrder() {
        assertTrue(diagnosticOrdererIdentifier.validates(getResource("xmls/encounters/providers_identifiers/diagnostic_order.xml",
                ResourceType.DiagnosticOrder)));
    }

    @Test
    public void shouldExtractProperDiagnosticOrdererReference() {
        List<String> references = diagnosticOrdererIdentifier.extractUrls(getResource
                ("xmls/encounters/providers_identifiers/diagnostic_order.xml", ResourceType.DiagnosticOrder));
        assertEquals(1, references.size());
        assertEquals("http://127.0.0.1:9997/providers/18.json", references.get(0));

        references = diagnosticOrdererIdentifier.extractUrls(getResource
                ("xmls/encounters/providers_identifiers/diagnostic_order_no_orderer.xml", ResourceType.DiagnosticOrder));
        assertNull(references);

    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        assertFalse(diagnosticOrdererIdentifier.validates(getResource
                ("xmls/encounters/providers_identifiers/encounter_with_valid_participant.xml", ResourceType.Encounter)));
    }

    private Resource getResource(String file, ResourceType resType) {
        ValidationSubject<AtomEntry<? extends Resource>> validationSubject = AtomFeedHelper.getAtomFeed(file, resType);
        return validationSubject.extract().getResource();
    }

}