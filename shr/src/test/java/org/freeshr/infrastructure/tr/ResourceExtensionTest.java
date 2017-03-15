package org.freeshr.infrastructure.tr;


import ca.uhn.fhir.model.api.ExtensionDt;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourceExtensionTest {

    @Test
    public void shouldDeSerializeMedicationWithExtension() throws Exception {
        String medicationJson = FileUtil.asString("jsons/medication_extn.json");
        IBaseResource resource = new FhirFeedUtil().getFhirContext().newJsonParser().parseResource(medicationJson);
        assertTrue("Deserialized resource should be of type Medication", resource instanceof Medication);
        Medication medication = (Medication) resource;
        List<Extension> extensions = medication.getExtension();
        assertEquals(2, extensions.size());
        List<Extension> medStrengthExtn = medication.getExtensionsByUrl("http://192.168.33.17:9080/openmrs/ws/rest/v1/tr/medication#med-extension-strength");
        assertEquals(1, medStrengthExtn.size());
        assertEquals("500 mg", medStrengthExtn.get(0).getValue().toString());
    }
}
