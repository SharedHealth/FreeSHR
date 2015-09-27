package org.freeshr.validations.resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.resource.ProcedureValidator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.junit.Assert.*;

public class ProcedureValidatorTest {

    private ProcedureValidator procedureValidator;

    @Before
    public void setUp() {
        procedureValidator = new ProcedureValidator();
    }

    @Test
    public void shouldValidateProcedure() {
        final FhirContext fhirContext = FhirContext.forDstu2();
        ca.uhn.fhir.model.dstu2.resource.Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_procedure.xml"), fhirContext);
        List<Procedure> procedures = FhirResourceHelper.findBundleResourcesOfType(bundle, Procedure.class);
        List<ShrValidationMessage> validationMessages = procedureValidator.validate(procedures.get(0));
        assertTrue(validationMessages.isEmpty());

    }

    @Test
    @Ignore
    public void shouldValidateDateInProcedure() {
        //TODO
    }

    @Test
    @Ignore
    public void shouldValidateDiagnosticReportResourceReference() {
        //TODO
    }

}