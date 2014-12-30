package org.freeshr.application.fhir;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.infrastructure.tr.ValueSetCodeValidator;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.*;
import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.Boolean;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
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
    private StructureValidator structureValidator;

    @Autowired
    private FhirMessageFilter fhirMessageFilter;

    private FhirSchemaValidator fhirSchemaValidator;

    @Autowired
    ValueSetCodeValidator valueSetCodeValidator;

    EncounterBundle encounterBundle;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setup() throws Exception {
        initMocks(this);
        fhirSchemaValidator = new FhirSchemaValidator(trConceptLocator, shrProperties);
        validator = new EncounterValidator(fhirMessageFilter, fhirSchemaValidator, resourceValidator,
                healthIdValidator, structureValidator);
        encounterBundle = EncounterBundleData.withValidEncounter();

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/drugs/3be99d23-e50d-41a6-ad8c-f6434e49f513"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));
    }

    @Test
    public void shouldValidateEncounterWhenInProperFormat() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounter.xml"));
        EncounterValidationResponse validate = validator.validate(encounterBundle);
        assertTrue(validate.isSuccessful());
    }

    @Test
    public void shouldFailIfConditionStatusIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/invalid_condition.xml"));
        EncounterValidationResponse response = validator.validate(encounterBundle);
        assertFalse(response.isSuccessful());
        assertEquals(1, response.getErrors().size());
        //assertEquals("Condition-status", response.getErrors().get(0).getField());
        assertEquals("Unknown", response.getErrors().get(0).getField());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConcept() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("invalid-eddb01eb-61fc-4f9e-aca5"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "Invalid code"));

        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/invalid_concept.xml"));
        assertFalse(validator.validate(encounterBundle).isSuccessful());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptReferenceTerms() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("INVALID_REFERENCE_TERM"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "Invalid code"));

        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/invalid_ref.xml"));
        assertFalse(validator.validate(encounterBundle).isSuccessful());
    }

    @Test
    public void shouldRejectEncounterWithMissingSystemForDiagnosis() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_system_missing.xml"));
        assertFalse(validator.validate(encounterBundle).isSuccessful());
    }

    @Test
    public void shouldRejectEncountersWithDiagnosisHavingAllInvalidSystems() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_system_invalid.xml"));
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        assertFalse(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldTreatFHIRWarningAsError() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_system_invalid.xml"));
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        assertFalse(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldRejectInvalidDiagnosisCategory() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_category_invalid.xml"));
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        assertFalse(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosticOrder() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(1)).verifiesSystem(anyString());
        assertTrue(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateSpecimenWithDiagnosticOrder() throws Exception {
        encounterBundle= EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,FileUtil.asString("xmls/encounters/diagnostic_order_with_specimen.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse= validator.validate(encounterBundle);
        verify(trConceptLocator,times(3)).verifiesSystem(anyString());

        assertTrue(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosticReport() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnostic_report.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(4)).verifiesSystem(anyString());
        assertTrue(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateConditionsToCheckIfCategoriesOtherThanChiefComplaintAreCoded() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/coded_and_noncoded_diagnosis.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(6)).verifiesSystem(anyString());
        assertFalse(encounterValidationResponse.isSuccessful());
        assertThat(encounterValidationResponse.getErrors().size(), is(3));
    }


    @Test
    public void shouldValidateCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounter_with_obs_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        assertTrue(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldInvalidateWrongCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounter_with_obs_invalid.xml"));
        when(trConceptLocator.validate(anyString(), eq("77405a73-b915-4a93-87a7-f29fe6697fb4-INVALID"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "Invalid code"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        List<Error> errors = encounterValidationResponse.getErrors();
        assertEquals(2, errors.size());
        assertEquals("Invalid code", errors.get(0).getReason());
        assertFalse(encounterValidationResponse.isSuccessful());

    }

    @Test
    public void shouldValidateIfTheHealthIdInTheEncounterContentIsNotSameAsTheOneExpected() {
        encounterBundle.setHealthId("1111222233334444555");
        EncounterValidationResponse response = validator.validate(encounterBundle);
        assertFalse(response.isSuccessful());
        assertThat(response.getErrors().size(), is(3));
        assertTrue(response.getErrors().get(0).getReason().contains("Health Id does not match"));
        assertTrue(response.getErrors().get(1).getReason().contains("Health Id does not match"));
        assertTrue(response.getErrors().get(2).getReason().contains("Health Id does not match"));
    }

    @Test
    public void shouldValidateEncounterTypeAgainstValueSet() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounter_with_valid_type.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(1)).verifiesSystem
                ("http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/79647ed4-a60e-4cf5-ba68-cf4d55956cba");
        verify(trConceptLocator, times(1)).verifiesSystem
                ("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type");
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "REG", "registration");
        assertTrue(encounterValidationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateMedicationPrescription() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse validationResponse = validator.validate(encounterBundle);
        assertTrue(validationResponse.isSuccessful());
    }

    @Test
    public void shouldValidatePrescriptionWithInvalidMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_invalid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);


        EncounterValidationResponse validationResponse = validator.validate(encounterBundle);
        assertFalse("Invalid medication prescription should have failed validation", validationResponse.isSuccessful());
        List<Error> invalidUrlError = CollectionUtils.filter(validationResponse.getErrors(), new CollectionUtils.Fn<Error, Boolean>() {
            @Override
            public Boolean call(Error e) {
                return e.getReason().equals(MedicationValidator.INVALID_MEDICATION_REFERENCE_URL);
            }
        });
        assertEquals("Should have found one invalid medication url", 2, invalidUrlError.size());

/*
        ResourceOrFeedDeserializer resourceOrFeedDeserializer= new ResourceOrFeedDeserializer();
        final String xml = FileUtil.asString("xmls/encounters/medication_prescription_invalid.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);

        for (AtomEntry<? extends Resource> atomEntry : feed.getEntryList()) {
            Resource resource = atomEntry.getResource();
            ResourceType resourceType = resource.getResourceType();
            Property medication = resource.getChildByName("medication");
            assertNotNull(medication);
            ResourceReference medicationRefValue = ((ResourceReference) medication.getValues().get(0));
            assertNotNull(medicationRefValue);

        }*/

    }

    @Test
    public void shouldValidatePrescriptionWithValidMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse validationResponse = validator.validate(encounterBundle);
        assertTrue("Medication prescription pass through validation", validationResponse.isSuccessful());
    }

    @Test
    public void shouldValidateRouteInMedicationPrescription(){
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        EncounterValidationResponse encounterValidationResponse = validator.validate(encounterBundle);
        verify(trConceptLocator, times(1)).verifiesSystem("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration");
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration","implant", "implant");
        assertTrue(encounterValidationResponse.isSuccessful());
    }


}
