package org.freeshr.application.fhir;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.infrastructure.tr.ValueSetCodeValidator;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.FhirMessageFilter;
import org.freeshr.validations.HapiEncounterValidator;
import org.freeshr.validations.bundle.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.utils.FileUtil.asString;
import static org.freeshr.validations.ValidationMessages.INVALID_DISPENSE_MEDICATION_REFERENCE_URL;
import static org.freeshr.validations.ValidationMessages.INVALID_MEDICATION_REFERENCE_URL;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997", "FACILITY_REGISTRY_URL=http://localhost:9997/facilities/", "PROVIDER_REGISTRY_URL=http://localhost:9997/providers/"})
public class EncounterValidatorIntegrationTest {

    public static final String HEALTH_ID = "98001046534";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    @Autowired
    ValueSetCodeValidator valueSetCodeValidator;
    EncounterBundle encounterBundle;
    @Autowired
    private HapiEncounterValidator validator;
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
        encounterBundle = EncounterBundleData.withValidEncounter();

//        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/drugs/3be99d23-e50d-41a6-ad8c-f6434e49f513"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(asString("jsons/medication_paracetamol.json"))));
//
//        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Quantity-Units"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(asString("jsons/code.json"))));
//
//        givenThat(get(urlEqualTo("/facilities/10000069.json"))
//                .withHeader("client_id", matching("18550"))
//                .withHeader("X-Auth-Token", matching("c6e6fd3a26313eb250e1019519af33e743808f5bb50428ae5423b8ee278e6fa5"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(asString("jsons/facility10000069.json"))));
//
//        givenThat(get(urlEqualTo("/facilities/100000603.json"))
//                .willReturn(aResponse()
//                        .withStatus(404)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(asString("jsons/facility100000603.json"))));

        //Patient 98001046534 Reference
        givenThat(get(urlEqualTo("/api/default/patients/" + HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/patient98001046534.json"))));

        //Facility 10019841 reference
        givenThat(get(urlEqualTo("/facilities/10019841.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/facility10019841.json"))));

        //terms for dengue fever
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/referenceterms/2f6z9872-4df1-438e-9d72-0a8b161d409b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/ref_term_dengue.json"))));


        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_dengue.json"))));

        //terms for complete blood count
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/referenceterms/CBC-4df1-438e-9d72-0a8b161d409b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/cbc_loinc_refTerm.json"))));
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/CBC-5206-11e5-ae6d-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/cbc_labSet_concept.json"))));

        //terms for complete blood count
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/referenceterms/Creatinine-4df1-438e-9d72-0a8b161d409b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/creatinine_loinc_refTerm.json"))));
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/Creatinine-5206-11e5-ae6d-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/creatinine_labtest_concept.json"))));

        //tr valueset relationship type
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Relationship-Type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_Relationship_type.json"))));

        //tr valueset routes of administration
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Route-of-Administration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_Routes_of_administration.json"))));

        //tr valueset medication forms
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Medication-Forms"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_medication_forms.json"))));

        //tr drug for Lactic Acid
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/drugs/23d7e743-75bd-4a25-8f34-bd849bd50394"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));


    }

    @Test
    public void shouldValidateEncounterIfItHasAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        for (Error error : response.getErrors()) {
            System.out.println(error);
        }
        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore("TODO. Although covered as part of FacilityValidatorTest Test")
    public void shouldFailIfNotAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/encounterWithInvalidFacility.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:d3cc23c3-1f12-4b89-a415-356feeba0690", FacilityValidator.INVALID_SERVICE_PROVIDER, response
                .getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore("TODO. Although covered as part of FacilityValidatorTest Test")
    public void shouldFailIfFacilityUrlIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/encounterWithInvalidFacilityUrl.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:d3cc23c3-1f12-4b89-a415-356feeba0690", FacilityValidator.INVALID_SERVICE_PROVIDER,
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    /**
     * Dependency: StructureValidator
     *
     * @throws Exception
     */
    @Test
    @Ignore("TODO: fix and move to StructureValidatorTest")
    public void shouldValidateEncounterWhenInProperFormat() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConcept() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidConcept.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:code",
                "Unable to validate code \"INVALID-07952dc2-5206-11e5-ae6d-0050568225ca\" in code system \"http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca\"",
                false, response);
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptReferenceTerms() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidRefTerm.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:code",
                "Unable to validate code \"INVALID-A90\" in code system \"http://localhost:9997/openmrs/ws/rest/v1/tr/referenceterms/2f6z9872-4df1-438e-9d72-0a8b161d409b\"",
                false, response);
        assertEquals(1, response.getErrors().size());
    }


    @Test
    public void shouldFailIfConditionStatusIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_localRefs_invalidCondition.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:clinicalStatus",
                "Coded value wrong is not in value set http://hl7.org/fhir/ValueSet/condition-clinical", true, response);
    }

    /**
     * Coding System can not be empty
     * Category must be one of the preferred
     * Clinical Status must be of the preferred
     *
     * @throws Exception
     */
    @Test
    public void shouldRejectEncounterWithInvalidDiagnosisCategoryAndStatusAndSystem() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_localRefs_invalidCondition.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding/f:system",
                "@value cannot be empty", false, response);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:category",
                "None of the codes are in the example value set http://hl7.org/fhir/ValueSet/condition-category", true, response);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:clinicalStatus",
                "Coded value wrong is not in value set http://hl7.org/fhir/ValueSet/condition-clinical", true, response);
        assertEquals(3, response.getErrors().size());
    }

    @Test
    @Ignore("Same as 'shouldRejectEncounterWithInvalidDiagnosisCategoryAndStatusAndSystem'")
    public void shouldTreatFHIRWarningAsError() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_localRefs_invalidCondition.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding/f:system",
                "@value cannot be empty", false, response);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:category",
                "None of the codes are in the example value set http://hl7.org/fhir/ValueSet/condition-category", true, response);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:clinicalStatus",
                "Coded value wrong is not in value set http://hl7.org/fhir/ValueSet/condition-clinical", true, response);
    }


    @Test
    public void shouldValidateDiagnosticOrder() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnosticOrder.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateFamilyMemberHistory() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_family_member_history.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldRejectInvalidRelationshipTypeInFamilyMemberHistory() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_family_member_history_relationship_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:FamilyMemberHistory/f:relationship",
                "Unable to validate code \"FT\" in code system \"http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Relationship-Type\"", true, response);
    }

    @Test
    public void shouldValidateMedicationOrderWithScheduledDate() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_scheduled_date.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateMedicationOrderWithCustomDosage() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_custom_dosage.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        debugEncounterValidationResponse(response);
        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateSpecimenWithDiagnosticOrder() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/diagnostic_order_with_specimen.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(3)).verifiesSystem(anyString());

        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateDiagnosticReport() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/diagnostic_report.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(4)).verifiesSystem(anyString());
        assertTrue(response.isSuccessful());
    }


    @Test
    @Ignore
    public void shouldValidateConditionsToCheckIfCategoriesOtherThanChiefComplaintAreCoded() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/coded_and_noncoded_diagnosis.xml"));
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
    @Ignore
    public void shouldValidateCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/encounter_with_obs_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldInvalidateWrongCodesInObservations() {
//        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
//                FileUtil.asString("xmls/encounters/encounter_with_obs_invalid.xml"));
//        when(trConceptLocator.validate(anyString(), eq("77405a73-b915-4a93-87a7-f29fe6697fb4-INVALID"),
//                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
//                "Invalid code 77405a73-b915-4a93-87a7-f29fe6697fb4-INVALID"));
//        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
//        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
//        EncounterValidationResponse response = validator.validate(validationContext);
//        assertFailureFromResponseErrors("/f:entry[3]/f:content/f:Observation/f:Observation/f:name/f:coding",
//                "Invalid code 77405a73-b915-4a93-87a7-f29fe6697fb4-INVALID", response.getErrors());
//        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore
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
    @Ignore
    public void shouldValidateEncounterTypeAgainstValueSet() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/encounter_with_valid_type.xml"));
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
    @Ignore
    public void shouldValidateMedicationPrescriptionWithInvalidMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", INVALID_MEDICATION_REFERENCE_URL,
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateRouteInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).verifiesSystem("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration");
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration", "implant",
                "implant");
        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateRouteInMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).validate("http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Route-of-Administration", "implant",
                "implant");
        assertTrue("Medication prescription pass through validation", response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateDispenseMedicationInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_substitution_type_reason.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Medication-prescription,Prescriber pass through validation", response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateSiteAndReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_route_valid.xml"));
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
    @Ignore
    public void shouldValidateDispenseAndAdditionalInstructionsInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_dispense_addinformation_valid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        verify(trConceptLocator, times(1)).validate("http://172.18.46.56:9080/openmrs/ws/rest/v1/tr/additional-instructions",
                "79647ed4-a60e-4cf5-ba68-cf4d55956xyz", "Take With Water");
        assertTrue("Should Validate Valid Encounter In MedicationPrescription", response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateInvalidDispenseInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_dispense_addinformation_invalid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:6dc6d0d9-9520-4015-87cb-ab0cfa7e4b50", INVALID_DISPENSE_MEDICATION_REFERENCE_URL,
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateSubstitutionTypeAndReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_substitution_type_reason.xml"));
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
    @Ignore
    public void shouldValidateMethodAndAsNeededXInMedicationPrescription() {

        /**
         * medication_prescription_route_valid.xml has
         * 2 medication prescribed with asNeeded (boolean true), and asNeeded with CodeableConcept
         *
         */
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_route_valid.xml"));
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
    @Ignore
    public void shouldValidateInvalidDosageQuantityInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/medication_prescription_invalid_dosage_quantity.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", "Invalid Dosage Quantity",
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateDischargeSummaryEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/discharge_summary_encounter.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateInvalidSchemaInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/discharge_summary_encounter_invalid_schema.xml"));

        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        List<Error> errors = response.getErrors();
        assertEquals(1, errors.size());
        assertEquals("Unknown", errors.get(0).getField());
        assertTrue("Should have failed for unknown ConditionStatus code", errors.get(0).getReason().contains("Unknown ConditionStatus " +
                "code 'foo-bar'"));
    }

    @Test
    @Ignore
    public void shouldValidateInvalidMedicationInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/discharge_summary_encounter_medication_invalid.xml"));

        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", "Invalid Medication Reference URL",
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateInvalidDosageQuantityInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/discharge_summary_dosage_quantity_invalid.xml"));
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", "Invalid Dosage Quantity",
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateInvalidCodeInDischargeSummaryEncounter() {
//        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
//        when(trConceptLocator.validate(anyString(), eq("a6e20fe1-4044-4ce7-8440-577f7f814765-invalid"),
//                anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error,
//                "Invalid code a6e20fe1-4044-4ce7-8440-577f7f814765-invalid"));
//
//        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
//                FileUtil.asString("xmls/encounters/discharge_summary_encounter_code_invalid.xml"));
//        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
//        EncounterValidationResponse response = validator.validate(validationContext);
//        assertFailureFromResponseErrors("/f:entry[2]/f:content/f:Observation/f:Observation/f:name/f:coding",
//                "Invalid code a6e20fe1-4044-4ce7-8440-577f7f814765-invalid", response.getErrors());
//        verify(trConceptLocator, times(5)).validate(anyString(), eq("a6e20fe1-4044-4ce7-8440-577f7f814765-invalid"), anyString());
//        assertEquals(5, response.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateMissingSystemCodeInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/discharge_summary_encounter_system_missing.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("/f:entry[24]/f:content/f:Observation/f:Observation/f:name/f:coding/f:system",
                "@value cannot be empty", response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    @Ignore
    public void shouldValidateProcedure() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/procedure/encounter_Procedure.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore
    public void shouldValidateInvalidEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/encounter_invalid_with_all_resources.xml"));
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


    @Ignore
    @Test
    public void shouldValidateEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_all_resources.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        debugEncounterValidationResponse(response);
        assertTrue(response.isSuccessful());
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

    private void debugEncounterValidationResponse(EncounterValidationResponse response) {
        for (Error error : response.getErrors()) {
            System.out.println("Reason : " + error.getReason() + "      Field: " + error.getField() + "      Type: " + error.getType());
        }
    }

    private void assertFailureInResponse(String field, String message, boolean partialSearch, EncounterValidationResponse response) {
        for (Error error : response.getErrors()) {
            if (error.getField().equals(field)) {
                boolean result = partialSearch ? error.getReason().startsWith(message) : error.getReason().equals(message);
                if (result) return;
            }
        }
        fail("Unable to find expected validation error with matching field and message:");
    }
}
