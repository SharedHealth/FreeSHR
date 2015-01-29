package org.freeshr.validations;

import org.freeshr.domain.ErrorMessageBuilder;
import org.freeshr.utils.AtomFeedHelper;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcedureValidatorTest {

    private ProcedureValidator procedureValidator;

    @Before
    public void setUp() {
        initMocks(this);
        procedureValidator = new ProcedureValidator(new DateValidator());
    }

    @Test
    public void shouldValidateProcedure() {

        ValidationSubject<AtomEntry<? extends Resource>> feed = AtomFeedHelper.getAtomFeed("xmls/encounters/procedure/encounter_Procedure.xml", ResourceType.Procedure);
        List<ValidationMessage> validationMessages = procedureValidator.validate(feed);
        assertTrue(validationMessages.isEmpty());

    }

    @Test
    public void shouldValidateDateInProcedure() {

        ValidationSubject<AtomEntry<? extends Resource>> feed = AtomFeedHelper.getAtomFeed("xmls/encounters/procedure/encounter_invalid_period_Procedure.xml", ResourceType.Procedure);
        List<ValidationMessage> validationMessages = procedureValidator.validate(feed);
        assertFalse(validationMessages.isEmpty());
        assertEquals(ErrorMessageBuilder.INVALID_PERIOD, validationMessages.get(0).getMessage());


    }

    @Test
    public void shouldValidateDiagnosticReportResourceReference() {

        ValidationSubject<AtomEntry<? extends Resource>> feed = AtomFeedHelper.getAtomFeed("xmls/encounters/procedure/encounter_invalid_report_reference_Procedure.xml", ResourceType.Procedure);
        List<ValidationMessage> validationMessages = procedureValidator.validate(feed);
        assertFalse(validationMessages.isEmpty());
        assertEquals(ErrorMessageBuilder.INVALID_DIAGNOSTIC_REPORT_REFERNECE, validationMessages.get(0).getMessage());

    }

}