package org.freeshr.validations.resource;

import ca.uhn.fhir.context.FhirContext;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.ShrValidationMessage;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Procedure;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.assertTrue;

public class ProcedureValidatorTest {

    private ProcedureValidator procedureValidator;

    @Before
    public void setUp() {
        procedureValidator = new ProcedureValidator();
    }

    @Test
    public void shouldValidateProcedure() {
        final FhirContext fhirContext = FhirContext.forDstu3();
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_procedure.xml"), fhirContext);
        List<Procedure> procedures = FhirResourceHelper.findBundleResourcesOfType(bundle, Procedure.class);
        List<ShrValidationMessage> validationMessages = procedureValidator.validate(procedures.get(0));
        assertTrue(validationMessages.isEmpty());

    }
}
