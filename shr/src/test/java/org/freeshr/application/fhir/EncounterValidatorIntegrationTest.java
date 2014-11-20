package org.freeshr.application.fhir;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.FhirSchemaValidator;
import org.freeshr.validations.HealthIdValidator;
import org.freeshr.validations.ResourceValidator;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class EncounterValidatorIntegrationTest {

    private EncounterValidator validator;

    @Mock
    private TRConceptLocator trConceptLocator;

    @Autowired
    private SHRProperties shrProperties;

    @Autowired
    private ResourceValidator resourceValidator;

    @Autowired
    private HealthIdValidator healthIdValidator;

    @Autowired
    private FhirMessageFilter fhirMessageFilter;

    private FhirSchemaValidator fhirSchemaValidator;

    EncounterBundle encounterBundle;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        fhirSchemaValidator = new FhirSchemaValidator(trConceptLocator, shrProperties);
        validator = new EncounterValidator(fhirMessageFilter, fhirSchemaValidator, resourceValidator, healthIdValidator );
        encounterBundle = EncounterBundleData.withValidEncounter();
    }

    @Test
    public void shouldValidateEncounterWhenInProperFormat() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/encounter.xml"));
        EncounterValidationResponse validate = validator.validate(encounterBundle);
        assertTrue(validate.isSuccessful());
    }

    @Test
    public void shouldFailIfConditionStatusIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/invalid_condition.xml"));
        EncounterValidationResponse response = validator.validate(encounterBundle);
        assertFalse(response.isSuccessful());
        assertEquals(1, response.getErrors().size());
        assertEquals("Condition-status", response.getErrors().get(0).getField());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConcept() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("invalid-eddb01eb-61fc-4f9e-aca5"), anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error, "Invalid code"));

        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/invalid_concept.xml"));
        assertFalse(validator.validate(encounterBundle).isSuccessful());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptReferenceTerms() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("INVALID_REFERENCE_TERM"), anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error, "Invalid code"));

        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/invalid_ref.xml"));
        assertFalse(validator.validate(encounterBundle).isSuccessful());
    }

    @Test
    public void shouldRejectEncounterWithMissingSystemForDiagnosis() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/diagnosis_system_missing.xml"));
        assertFalse(validator.validate(encounterBundle).isSuccessful());
    }

    @Test
    public void shouldRejectEncountersWithDiagnosisHavingAllInvalidSystems() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/diagnosis_system_invalid.xml"));
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        assertFalse(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldTreatFHIRWarningAsError() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/diagnosis_system_invalid.xml"));
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        assertFalse(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldRejectInvalidDiagnosisCategory() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/diagnosis_category_invalid.xml"));
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        assertFalse(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosticOrder() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(1)).verifiesSystem(anyString());
        assertTrue(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosticReport() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/diagnostic_report.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(3)).verifiesSystem(anyString());
        assertTrue(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateConditionsToCheckIfCategoriesOtherThanChiefComplaintAreCoded() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID, FileUtil.asString("xmls/encounters/coded_and_noncoded_diagnosis.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(6)).verifiesSystem(anyString());
        assertFalse(encounterValidationResponse.isSuccessful());
        assertThat(encounterValidationResponse.getErrors().size(), is(3));
    }

    @Test
    public void shouldValidateIfTheHealthIdInTheEncounterContentIsSameAsTheOneExpected() {
        encounterBundle.setHealthId("1111222233334444555");
        EncounterValidationResponse response = validator.validate(encounterBundle);
        assertFalse(response.isSuccessful());
        assertThat(response.getErrors().size(), is(1));
        verify(trConceptLocator, never()).verifiesSystem(anyString());
    }

}
