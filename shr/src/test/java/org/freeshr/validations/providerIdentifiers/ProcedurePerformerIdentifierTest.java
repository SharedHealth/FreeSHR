package org.freeshr.validations.providerIdentifiers;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class ProcedurePerformerIdentifierTest {

    private ProcedurePerformerIdentifier procedurePerformerIdentifier;
    private final FhirContext fhirContext = FhirContext.forDstu3();

    @Before
    public void setUp() {
        procedurePerformerIdentifier = new ProcedurePerformerIdentifier();
    }

    @Test
    public void shouldValidateResourceOfTypeProcedure() {
        Procedure procedure = new Procedure();
        assertTrue(procedurePerformerIdentifier.canValidate(procedure));
    }

    @Test
    public void shouldExtractProperProcedurePerformerReferences() {
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_procedure.xml"), fhirContext);
        List<Procedure> procedures = FhirResourceHelper.findBundleResourcesOfType(bundle, Procedure.class);
        List<Reference> providerReferences = procedurePerformerIdentifier.getProviderReferences(procedures.get(0));
        assertEquals(1, providerReferences.size());
        assertEquals("http://localhost:9997/providers/19.json", providerReferences.get(0).getReference());
    }

    @Test
    public void shouldNotValidateResourceOfOtherType() {
        Encounter enc = new Encounter();
        assertFalse(procedurePerformerIdentifier.canValidate(enc));
    }



}
