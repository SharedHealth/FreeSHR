package org.freeshr.validations.resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.LenientErrorHandler;
import org.freeshr.application.fhir.TRConceptValidator;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.UrlValidator;
import org.hl7.fhir.dstu3.hapi.validation.IValidationSupport;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.freeshr.utils.BundleHelper.parseBundle;
import static org.freeshr.validations.ValidationMessages.INVALID_DOSAGE_QUANTITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class ImmunizationValidatorTest {

    @Mock
    private TRConceptValidator conceptValidator;

    @Before
    public void setup() throws Exception {
        initMocks(this);

    }

    private ImmunizationValidator getValidator() {
        DoseQuantityValidator doseQuantityValidator = new DoseQuantityValidator(conceptValidator, new FhirFeedUtil());
        return new ImmunizationValidator(doseQuantityValidator, new UrlValidator());
    }

    @Test
    public void shouldValidateImmunization() throws Exception {
        final FhirContext fhirContext = FhirContext.forDstu3();
        fhirContext.setParserErrorHandler(new LenientErrorHandler().setErrorOnInvalidValue(false));
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_immunization.xml"), fhirContext);
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);
        when(conceptValidator.isCodeSystemSupported(any(FhirContext.class), anyString())).thenReturn(true);
        ImmunizationValidator immunizationValidator = getValidator();
        List<ShrValidationMessage> validationMessages = immunizationValidator.validate(immunizations.get(0));
        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void shouldRejectInvalidDoseQuantityType() {
        final FhirContext fhirContext = FhirContext.forDstu3();
        Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_immunization.xml"), fhirContext);
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);
        when(conceptValidator.isCodeSystemSupported(any(FhirContext.class), anyString())).thenReturn(true);
        when(conceptValidator.validateCode(any(FhirContext.class), anyString(), anyString(), anyString())).thenReturn(mockValidationResult());
        ImmunizationValidator immunizationValidator = getValidator();

        List<ShrValidationMessage> validationMessages = immunizationValidator.validate(immunizations.get(0));
        assertEquals(1, validationMessages.size());
        assertEquals(INVALID_DOSAGE_QUANTITY + ":Immunization:urn:uuid:554e13d9-25f9-4802-8f21-669249bf51be", validationMessages.get(0).getMessage());
    }

    private IValidationSupport.CodeValidationResult mockValidationResult() {
        return new IValidationSupport.CodeValidationResult(ValidationMessage.IssueSeverity.ERROR, "Invalid Code");
    }
}
