package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseResource;
import static org.junit.Assert.*;

public class MedicationPrescriberIdentifierTest {

    private MedicationPrescriberIdentifier medicationPrescriberIdentifier;

    @Before
    public void setUp() {
        medicationPrescriberIdentifier = new MedicationPrescriberIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeMedicationPrescription() {
        MedicationOrder order = new MedicationOrder();
        assertTrue(medicationPrescriberIdentifier.validates(order));
    }

    @Test
    public void shouldExtractProperMedicationPrescriptionPerformerReference() {
        final FhirContext fhirContext = FhirContext.forDstu2();
        IBaseResource medicationOrder = parseResource(FileUtil.asString("xmls/encounters/dstu2/example_medication_order.xml"), fhirContext);
        IDatatype medication = ((MedicationOrder) medicationOrder).getMedication();
        List<ResourceReferenceDt> providerReferences = medicationPrescriberIdentifier.getProviderReferences((IResource) medicationOrder);
        assertEquals(1, providerReferences.size());
        assertEquals("Practitioner/f006", providerReferences.get(0).getReference().getValue());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Encounter encounter = new Encounter();
        assertFalse(medicationPrescriberIdentifier.validates(encounter));
    }

}