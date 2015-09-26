package org.freeshr.application.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.DoseQuantityValidator;
import org.freeshr.validations.ImmunizationValidator;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.UrlValidator;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.terminologies.ITerminologyServices;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
        final FhirContext fhirContext = FhirContext.forDstu2();
        ca.uhn.fhir.model.dstu2.resource.Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_immunization.xml"), fhirContext);
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        ImmunizationValidator immunizationValidator = getValidator();
        List<ShrValidationMessage> validationMessages = immunizationValidator.validate(immunizations.get(0));
        assertTrue(validationMessages.isEmpty());
    }

    @Test
    public void shouldRejectInvalidDoseQuantityType() {
        final FhirContext fhirContext = FhirContext.forDstu2();
        ca.uhn.fhir.model.dstu2.resource.Bundle bundle = parseBundle(FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_immunization.xml"), fhirContext);
        List<Immunization> immunizations = FhirResourceHelper.findBundleResourcesOfType(bundle, Immunization.class);
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), anyString(), anyString())).thenReturn(mockValidationResult());
        ImmunizationValidator immunizationValidator = getValidator();

        List<ShrValidationMessage> validationMessages = immunizationValidator.validate(immunizations.get(0));
        assertEquals(1, validationMessages.size());
        assertEquals(INVALID_DOSAGE_QUANTITY + ":Immunization:urn:uuid:554e13d9-25f9-4802-8f21-669249bf51be", validationMessages.get(0).getMessage());
    }

    private ITerminologyServices.ValidationResult mockValidationResult() {
        return new ITerminologyServices.ValidationResult(OperationOutcome.IssueSeverity.ERROR, "Invalid Code");
    }
}
