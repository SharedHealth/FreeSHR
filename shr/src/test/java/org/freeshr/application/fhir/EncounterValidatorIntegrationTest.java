package org.freeshr.application.fhir;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.infrastructure.tr.ValueSetCodeValidator;
import org.freeshr.utils.FileUtil;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.*;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.validations.ValidationMessages.INVALID_DISPENSE_MEDICATION_REFERENCE_URL;
import static org.freeshr.validations.ValidationMessages.INVALID_MEDICATION_REFERENCE_URL;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class EncounterValidatorIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    @Autowired
    ValueSetCodeValidator valueSetCodeValidator;
    EncounterBundle encounterBundle;
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
    @Autowired
    private ProviderValidator providerValidator;
    @Autowired
    private FacilityValidator facilityValidator;

    private EncounterValidationContext validationContext;


    @Before
    public void setup() throws Exception {
        initMocks(this);
        FhirSchemaValidator fhirSchemaValidator = new FhirSchemaValidator(trConceptLocator, shrProperties);
        validator = new EncounterValidator(fhirMessageFilter, fhirSchemaValidator, resourceValidator,
                healthIdValidator, structureValidator, providerValidator, facilityValidator);
        encounterBundle = EncounterBundleData.withValidEncounter();

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/drugs/3be99d23-e50d-41a6-ad8c-f6434e49f513"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Quantity-Units"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/code.json"))));

        givenThat(get(urlEqualTo("/facilities/10000069.json"))
                .withHeader("client_id", matching("18550"))
                .withHeader("X-Auth-Token", matching("c6e6fd3a26313eb250e1019519af33e743808f5bb50428ae5423b8ee278e6fa5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility10000069.json"))));

        givenThat(get(urlEqualTo("/facilities/100000603.json"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility100000603.json"))));

    }

    @Test
    public void shouldValidateEncounterIfItHasAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounterWithValidFacility.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        for (Error error : response.getErrors()) {
            System.out.println(error);
        }
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldFailIfNotAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounterWithInvalidFacility.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:d3cc23c3-1f12-4b89-a415-356feeba0690", FacilityValidator.INVALID_SERVICE_PROVIDER, response
                .getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldFailIfFacilityUrlIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounterWithInvalidFacilityUrl.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:d3cc23c3-1f12-4b89-a415-356feeba0690", FacilityValidator.INVALID_SERVICE_PROVIDER,
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateEncounterWhenInProperFormat() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounterWithDiagnosis.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldFailIfConditionStatusIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/invalid_condition.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertEquals(1, response.getErrors().size());
        assertEquals("Unknown", response.getErrors().get(0).getField());
        assertTrue("Should have failed for unknown ConditionStatus code", response.getErrors().get(0).getReason().contains
                ("ConditionStatus code"));
    }

    @Test
    public void shouldRejectEncounterWithInvalidConcept() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("invalid-eddb01eb-61fc-4f9e-aca5"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "Invalid code invalid-eddb01eb-61fc-4f9e-aca5"));

        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/invalid_concept.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "Invalid code " +
                "invalid-eddb01eb-61fc-4f9e-aca5", response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptReferenceTerms() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("INVALID_REFERENCE_TERM"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "INVALID_REFERENCE_TERM"));

        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/invalid_ref.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:code/f:coding", "INVALID_REFERENCE_TERM", response
                .getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldRejectEncounterWithMissingSystemForDiagnosis() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_system_missing.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:category/f:coding/f:system", "@value cannot be " +
                "empty", response.getErrors());
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:category",
                "None of the codes are in the expected value set http://hl7.org/fhir/vs/condition-category (http://hl7" +
                        ".org/fhir/vs/condition-category)", response.getErrors());
        assertEquals(2, response.getErrors().size());
    }

    @Test
    public void shouldRejectEncountersWithDiagnosisHavingAllInvalidSystems() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_system_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:category/f:coding",
                "Unknown Code System http://hl7.org/fhir/condition-category-invalid", response.getErrors());
        assertEquals(2, response.getErrors().size());
    }

    @Test
    public void shouldTreatFHIRWarningAsError() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_system_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:category",
                "None of the codes are in the expected value set http://hl7.org/fhir/vs/condition-category (http://hl7" +
                        ".org/fhir/vs/condition-category)",
                response.getErrors());
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:category/f:coding",
                "Unknown Code System http://hl7.org/fhir/condition-category-invalid",
                response.getErrors());
        assertEquals(2, response.getErrors().size());
    }

    @Test
    public void shouldRejectInvalidDiagnosisCategory() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnosis_category_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:category",
                "None of the codes are in the expected value set http://hl7.org/fhir/vs/condition-category (http://hl7" +
                        ".org/fhir/vs/condition-category)",
                response.getErrors());
        assertFailureFromResponseErrors("/f:entry/f:content/f:Condition/f:Condition/f:category/f:coding",
                "Unknown Code (http://hl7.org/fhir/condition-category#invalid)",
                response.getErrors());
        assertEquals(2, response.getErrors().size());

    }

    @Test
    public void shouldValidateDiagnosticOrder() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).verifiesSystem(anyString());
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateSpecimenWithDiagnosticOrder() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnostic_order_with_specimen.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(3)).verifiesSystem(anyString());

        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosticReport() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/diagnostic_report.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(4)).verifiesSystem(anyString());
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateConditionsToCheckIfCategoriesOtherThanChiefComplaintAreCoded() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/coded_and_noncoded_diagnosis.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        List<Error> errors = response.getErrors();
        assertThat(errors.size(), is(3));
        assertFailureFromResponseErrors("urn:5f982a33-4454-4b74-9236-b8157aa8effd", "Viral pneumonia 785857", errors);
        assertFailureFromResponseErrors("urn:5f982a33-4454-4b74-9236-b8157aa8e678", "Viral pneumonia 785857", errors);
        assertFailureFromResponseErrors("urn:9826cf0c-66d6-4e33-bed1-91381ab200b5", "Moderate", errors);
    }


    @Test
    public void shouldValidateCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounter_with_obs_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldInvalidateWrongCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounter_with_obs_invalid.xml"));
        when(trConceptLocator.validate(anyString(), eq("77405a73-b915-4a93-87a7-f29fe6697fb4-INVALID"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "Invalid code 77405a73-b915-4a93-87a7-f29fe6697fb4-INVALID"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry[3]/f:content/f:Observation/f:Observation/f:name/f:coding",
                "Invalid code 77405a73-b915-4a93-87a7-f29fe6697fb4-INVALID", response.getErrors());
        assertEquals(1, response.getErrors().size());

    }

    @Test
    public void shouldValidateIfTheHealthIdInTheEncounterContentIsNotSameAsTheOneExpected() {
        encounterBundle.setHealthId("1111222233334444555");
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("healthId", "Patient's Health Id does not match.", response.getErrors());
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
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).verifiesSystem
                ("http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/79647ed4-a60e-4cf5-ba68-cf4d55956cba");
        verify(trConceptLocator, times(1)).verifiesSystem
                ("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type");
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type",
                "REG", "registration");
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateMedicationPrescription() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateMedicationPrescriptionWithInvalidMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", INVALID_MEDICATION_REFERENCE_URL,
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateRouteInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).verifiesSystem("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration");
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration", "implant",
                "implant");
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateRouteInMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration", "implant",
                "implant");
        assertTrue("Medication prescription pass through validation", response.isSuccessful());
    }

    @Test
    public void shouldValidateDispenseMedicationInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_substitution_type_reason.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Medication-prescription,Prescriber pass through validation", response.isSuccessful());
    }

    @Test
    public void shouldValidateSiteAndReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_route_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).validate("http://172.18.46.56:9080/openmrs/ws/rest/v1/tr/vs/dosageInstruction-site",
                "181220002", "Entire oral cavity");
        verify(trConceptLocator, times(1)).validate("http://172.18.46.56:9080/openmrs/ws/rest/v1/tr/vs/prescription-reason", "38341003",
                "High blood pressure");
        assertTrue(response.isSuccessful());

    }

    @Test
    public void shouldValidateDispenseAndAdditionalInstructionsInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_dispense_addinformation_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).validate("http://172.18.46.56:9080/openmrs/ws/rest/v1/tr/additional-instructions",
                "79647ed4-a60e-4cf5-ba68-cf4d55956xyz", "Take With Water");
        assertTrue("Should Validate Valid Encounter In MedicationPrescription", response.isSuccessful());
    }

    @Test
    public void shouldValidateInvalidDispenseInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_dispense_addinformation_invalid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:6dc6d0d9-9520-4015-87cb-ab0cfa7e4b50", INVALID_DISPENSE_MEDICATION_REFERENCE_URL,
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateSubstitutionTypeAndReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_substitution_type_reason.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).validate("http://172.18.46.56:9080/openmrs/ws/rest/v1/tr/vs/substitution-type", "291220002",
                "Paracetamol");
        verify(trConceptLocator, times(1)).validate("http://172.18.46.56:9080/openmrs/ws/rest/v1/tr/vs/substitution-reason", "301220005"
                , "Paracetamol can be taken in place of this drug");
        assertTrue(response.isSuccessful());


    }


    @Test
    public void shouldValidateMethodAndAsNeededXInMedicationPrescription() {

        /**
         * medication_prescription_route_valid.xml has
         * 2 medication prescribed with asNeeded (boolean true), and asNeeded with CodeableConcept
         *
         */
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_route_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).validate("http://172.18.46" +
                        ".56:9080/openmrs/ws/rest/v1/tr/concepts/79647ed4-a60e-4cf5-ba68-cf4d55956cba",
                "79647ed4-a60e-4cf5-ba68-cf4d55956cba", "Hemoglobin");
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/administration-method-codes",
                "320276009", "Salmeterol+fluticasone 25/250ug inhaler");
        assertTrue(response.isSuccessful());

    }

    @Test
    public void shouldValidateInvalidDosageQuantityInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/medication_prescription_invalid_dosage_quantity.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", "Invalid Dosage Quantity",
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateDischargeSummaryEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/discharge_summary_encounter.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateInvalidSchemaInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/discharge_summary_encounter_invalid_schema.xml"));

        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        List<Error> errors = response.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Unknown", errors.get(0).getField());
        assertTrue("Should have failed for unknown ConditionStatus code", errors.get(0).getReason().contains("Unknown ConditionStatus " +
                "code 'foo-bar'"));
    }

    @Test
    public void shouldValidateInvalidMedicationInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/discharge_summary_encounter_medication_invalid.xml"));

        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", "Invalid Medication Reference URL",
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateInvalidDosageQuantityInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/discharge_summary_dosage_quantity_invalid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", "Invalid Dosage Quantity",
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateInvalidCodeInDischargeSummaryEncounter() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("a6e20fe1-4044-4ce7-8440-577f7f814765-invalid"),
                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
                "Invalid code a6e20fe1-4044-4ce7-8440-577f7f814765-invalid"));

        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/discharge_summary_encounter_code_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry[2]/f:content/f:Observation/f:Observation/f:name/f:coding",
                "Invalid code a6e20fe1-4044-4ce7-8440-577f7f814765-invalid", response.getErrors());
        verify(trConceptLocator, times(5)).validate(anyString(), eq("a6e20fe1-4044-4ce7-8440-577f7f814765-invalid"), anyString());
        assertEquals(5, response.getErrors().size());
    }

    @Test
    public void shouldValidateMissingSystemCodeInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/discharge_summary_encounter_system_missing.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry[24]/f:content/f:Observation/f:Observation/f:name/f:coding/f:system",
                "@value cannot be empty", response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateProcedure() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/procedure/encounter_Procedure.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateInvalidEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/encounter_invalid_with_all_resources.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        List<Error> errors = response.getErrors();
        assertFailureFromResponseErrors("urn:07f02524-7647-43c1-a579-0c2c80f285ed", "Invalid Service Provider",
                errors);
        assertFailureFromResponseErrors("urn:07f02524-7647-43c1-a579-0c2c80f285ed", "Invalid Provider URL in encounter",
                errors);
        assertFailureFromResponseErrors("urn:06a87681-68dd-455d-8dd3-5d4f34842905", "Invalid Provider URL in diagnosticreport",
                errors);
        assertFailureFromResponseErrors("urn:299531be-4e43-4349-9eb0-a48213e10692", "Invalid Dosage Quantity",
                errors);
        assertFailureFromResponseErrors("7urn:b9b8da08-1d9a-4968-b5be-0d47e518b2ec", "Invalid Period",
                errors);
    }

    private void assertFailureFromResponseErrors(String fieldName, String reason, List<Error> errors) {
        for (Error error : errors) {
            if (error.getReason().equals(reason)) {
                assertEquals(reason, error.getReason());
                return;
            }
        }
        fail(String.format("Couldn't find expected error with fieldName [%s] reason [%s]", fieldName, reason));
    }
}
