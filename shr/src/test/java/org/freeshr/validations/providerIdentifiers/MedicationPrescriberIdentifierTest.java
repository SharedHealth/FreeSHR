package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MedicationPrescriberIdentifierTest {

    private MedicationPrescriberIdentifier medicationPrescriberIdentifier;

    @Before
    public void setUp() {
        medicationPrescriberIdentifier = new MedicationPrescriberIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeMedicationPrescription() {
        MedicationRequest medicationRequest = new MedicationRequest();
        assertTrue(medicationPrescriberIdentifier.canValidate(medicationRequest));
    }

    @Test
    public void shouldExtractProperMedicationPrescriptionPerformerReference() {
        final FhirContext fhirContext = FhirContext.forDstu3();
        MedicationRequest medicationRequest = (MedicationRequest) parseResource(FileUtil.asString("xmls/encounters/stu3/example_medication_request.xml"), fhirContext);
        List<Reference> providerReferences = medicationPrescriberIdentifier.getProviderReferences(medicationRequest);
        assertEquals(1, providerReferences.size());
        assertEquals("Practitioner/f006", providerReferences.get(0).getReference());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Encounter encounter = new Encounter();
        assertFalse(medicationPrescriberIdentifier.canValidate(encounter));
    }

}
