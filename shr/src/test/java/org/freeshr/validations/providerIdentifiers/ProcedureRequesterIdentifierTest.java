package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProcedureRequesterIdentifierTest {

    private ProcedureRequesterIdentifier procedureRequesterIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu3();

    @Before
    public void setUp() {
        procedureRequesterIdentifier = new ProcedureRequesterIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeProcedureRequest() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_procedure_request_for_lab_with_specimen.xml"), fhirContext);
        List<ProcedureRequest> requests = FhirResourceHelper.findBundleResourcesOfType(bundle, ProcedureRequest.class);
        assertTrue(procedureRequesterIdentifier.canValidate(requests.get(0)));
    }

    @Test
    public void shouldExtractProperProcedureRequesterReference() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_procedure_request_for_lab_with_specimen.xml"), fhirContext);
        List<ProcedureRequest> requests = FhirResourceHelper.findBundleResourcesOfType(bundle, ProcedureRequest.class);
        List<Reference> providerReferences = procedureRequesterIdentifier.getProviderReferences(requests.get(0));
        assertEquals(1, providerReferences.size());
        assertEquals("http://localhost:9997/providers/18.json", providerReferences.get(0).getReference());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_procedure_request_for_lab_with_specimen.xml"), fhirContext);
        List<Encounter> encounters = FhirResourceHelper.findBundleResourcesOfType(bundle, Encounter.class);
        assertFalse(procedureRequesterIdentifier.canValidate(encounters.get(0)));
    }

}
