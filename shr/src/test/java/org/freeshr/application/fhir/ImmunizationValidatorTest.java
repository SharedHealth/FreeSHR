package org.freeshr.application.fhir;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.validations.DoseQuantityValidator;
import org.freeshr.validations.ImmunizationValidator;
import org.freeshr.validations.UrlValidator;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.freeshr.domain.ErrorMessage.INVALID_DOSAGE_QUANTITY;
import static org.freeshr.utils.AtomFeedHelper.getAtomFeed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class ImmunizationValidatorTest {

    @Mock
    private TRConceptLocator trConceptLocator;

    @Before
    public void setup() throws Exception {
        initMocks(this);

    }

    private ImmunizationValidator getValidator() {
        DoseQuantityValidator doseQuantityValidator = new DoseQuantityValidator(trConceptLocator);
        return new ImmunizationValidator(doseQuantityValidator, new UrlValidator());
    }

    @Test
    public void shouldValidateImmunization() throws Exception {
        ValidationSubject<AtomEntry<? extends Resource>> feed = getAtomFeed("xmls/encounters/immunization/immunization_valid.xml");

        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        ImmunizationValidator immunizationValidator = getValidator();

        List<ValidationMessage> validationMessages = immunizationValidator.validate(feed);

        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void shouldRejectInvalidDoseQuantityType() {

        ValidationSubject<AtomEntry<? extends Resource>> feed = getAtomFeed("xmls/encounters/immunization/immunization_invalid_dose_quantity.xml");

        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("INVALID-CODE"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "Invalid code"));

        ImmunizationValidator immunizationValidator = getValidator();

        List<ValidationMessage> validationMessasges = immunizationValidator.validate(feed);

        assertEquals(1,validationMessasges.size());
        assertEquals(INVALID_DOSAGE_QUANTITY,validationMessasges.get(0).getMessage());
    }
}
