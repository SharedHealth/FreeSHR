package org.freeshr.validations;

import org.freeshr.utils.AtomFeedHelper;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.freeshr.utils.AtomFeedHelper.getAtomFeed;
import static org.junit.Assert.*;

public class ProcedureValidatorTest {

    private ProcedureValidator procedureValidator;

    @Before
    public void setUp() {
        procedureValidator = new ProcedureValidator();
    }

    @Test
    public void shouldValidateProcedure() {

        ValidationSubject<AtomEntry<? extends Resource>> feed = getAtomFeed("xmls/encounters/procedure/encounter_Procedure.xml",
                ResourceType.Procedure);
        List<ValidationMessage> validationMessages = procedureValidator.validate(feed);
        assertTrue(validationMessages.isEmpty());

    }

    @Test
    public void shouldValidateDateInProcedure() {

        ValidationSubject<AtomEntry<? extends Resource>> feed = getAtomFeed
                ("xmls/encounters/procedure/encounter_invalid_period_Procedure.xml", ResourceType.Procedure);
        List<ValidationMessage> validationMessages = procedureValidator.validate(feed);
        assertFalse(validationMessages.isEmpty());
        assertEquals(ValidationMessages.INVALID_PERIOD, validationMessages.get(0).getMessage());


    }

    @Test
    public void shouldValidateDiagnosticReportResourceReference() {

        ValidationSubject<AtomEntry<? extends Resource>> feed = getAtomFeed
                ("xmls/encounters/procedure/encounter_invalid_report_reference_Procedure.xml", ResourceType.Procedure);
        List<ValidationMessage> validationMessages = procedureValidator.validate(feed);
        assertFalse(validationMessages.isEmpty());
        assertEquals(ValidationMessages.INVALID_DIAGNOSTIC_REPORT_REFERENCE, validationMessages.get(0).getMessage());

    }

}