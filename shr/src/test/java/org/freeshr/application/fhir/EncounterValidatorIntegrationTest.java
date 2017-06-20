package org.freeshr.application.fhir;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.ehcache.CacheManager;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.HapiEncounterValidator;
import org.freeshr.validations.bundle.FacilityValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.util.ValidationFailureTestHelper.assertFailureInResponse;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
@TestPropertySource(properties = {"MCI_SERVER_URL=http://localhost:9997", "FACILITY_REGISTRY_URL=http://localhost:9997/facilities/", "PROVIDER_REGISTRY_URL=http://localhost:9997/providers/"})
public class EncounterValidatorIntegrationTest {

    public static final String HEALTH_ID = "98001046534";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    @Autowired
    private HapiEncounterValidator validator;
    private EncounterValidationContext validationContext;
    EncounterBundle encounterBundle;

    @Before
    public void setup() throws Exception {
        encounterBundle = EncounterBundleData.withValidEncounter();
        //encounter-type
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/encounter-type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/encounter-type-case-insensitive.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Order-Type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/order-type-case-insensitive.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/condition-category"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/condition-category-case-insensitive.json"))));


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

        //leg anatomy concept
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/Leg-5206-11e5-io02-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/leg_anatomy_concept.json"))));

        //terms for creatinine

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

        //Dosing instruction as directed
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/1101"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_as_directed.json"))));

        //tr valueset routes of administration
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Route-of-Administration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_Routes_of_administration.json"))));

        //tr valueset Immunization-Reason
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Immunization-Reason"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_immunization_reason.json"))));

        //tr valueset medication forms
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/Medication-Forms"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_medication_forms.json"))));

        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/drugs/23d7e743-75bd-4a25-8f34-bd849bd50394"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/medication_paracetamol.json"))));

        //Lab test concept for hemoglobin in blood
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/07a9e3a1-5206-11e5-ae6d-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_hemoglobin_in_blood.json"))));

        //concept for viral pneumonia
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/eddb01eb-61fc-4f9e-aca5-e44193509f35"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_viral_pneumonia.json"))));

        //Temperature concept for vitals
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/5dd8f02e-3cda-40f2-9c70-b1a9c91ff1da"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_temperature.json"))));

        //Value set for order reason
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/order-reason"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_medication_request_reason.json"))));

        //dosage instruction Entire oral cavity
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/dosage-instruction-site"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_dosage_instruction_sites_entire_oral_cavity.json"))));

        //valueset additional-instruction take with water
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/additional-instructions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_additional_instruction_take_with_water.json"))));

        //valueset substitution-type paracetamol
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/substitution-type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_substitution_type_paracetamol.json"))));

        //valueset substitution-reason paracetamol
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/substitution-reason"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_substitution_reason_paracetamol_.json"))));

        //valueset administration method codes
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/administration-method-codes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/trValueset_administration-method-codes.json"))));

        //concept for hemoglobin
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/79647ed4-a60e-4cf5-ba68-cf4d55956cba"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_for_hemoglobin.json"))));


        //concept for colposcopy
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/079f6b0e-5206-11e5-ae6d-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_colposcopy.json"))));

        //concept for Vibrio Cholera
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/concepts/067c248c-5206-11e5-ae6d-0050568225ca"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/concept_vibrio_cholera.json"))));

        givenThat(get(urlPathMatching("/providers/"))
                .willReturn(aResponse()
                        .withStatus(200)));


    }

    @After
    public void tearDown() throws Exception {
        CacheManager.getInstance().clearAll();
    }

    @Test
    public void shouldValidateEncounterIfItHasAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnoses_with_local_refs.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        for (Error error : response.getErrors()) {
            System.out.println(error.getField());
            System.out.println(error.getType());
            System.out.println(error.getReason());
        }
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldFailIfNotAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_invalid_facility.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        String expectedErrorReason = FacilityValidator.INVALID_SERVICE_PROVIDER + ":http://127.0.0.1:9997/facilities/100000603.json";
        assertFailureInResponse("Encounter", expectedErrorReason, false, response);
    }

    @Test
    public void shouldFailIfFacilityUrlIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_invalid_facility_url.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("Encounter", FacilityValidator.INVALID_SERVICE_PROVIDER, true,
                response);
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateEncounterWhenInProperFormat() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnoses_with_local_refs.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldRejectEncounterWithInvalidConcept() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidConcept.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.code.coding[2]",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca], code[INVALID-07952dc2-5206-11e5-ae6d-0050568225ca]",
                false, response);
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptReferenceTerms() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidRefTerm.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("Bundle.entry[3].resource.code.coding[1]",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/referenceterms/2f6z9872-4df1-438e-9d72-0a8b161d409b], code[INVALID-A90]",
                false, response);
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldFailIfConditionStatusIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_localRefs_invalidCondition.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertFailureInResponse("Bundle.entry[3].resource.clinicalStatus",
                "The value provided ('wrong') is not in the value set http://hl7.org/fhir/ValueSet/condition-clinical (http://hl7.org/fhir/ValueSet/condition-clinical, and a code is required from this value set", true, response);
    }

    /**
     * Category must be one from given valueset
     * Clinical Status must be of the preferred
     *
     * @throws Exception
     */
    @Test
    public void shouldRejectEncounterWithInvalidDiagnosisCategoryAndStatusAndSystem() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_localRefs_invalidCondition.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(4, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.category.coding",
                "Unknown code: http://hl7.org/fhir/condition-category / invalid", false, response);
        assertFailureInResponse("Bundle.entry[4].resource.category.coding",
                "Unknown code: http://hl7.org/fhir/condition-category / invalid", false, response);
        assertFailureInResponse("Bundle.entry[3].resource.clinicalStatus",
                "The value provided ('wrong') is not in the value set http://hl7.org/fhir/ValueSet/condition-clinical (http://hl7.org/fhir/ValueSet/condition-clinical, and a code is required from this value set", true, response);
        assertFailureInResponse("Bundle.entry[4].resource.clinicalStatus",
                "The value provided ('wrong') is not in the value set http://hl7.org/fhir/ValueSet/condition-clinical (http://hl7.org/fhir/ValueSet/condition-clinical, and a code is required from this value set", true, response);
    }

    @Test
    public void shouldRejectEncounterWithConditionWithoutCode() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_condition__missing_code.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.code.coding",
                "There must be a code in condition", false, response);
    }

    @Test
    public void shouldRejectEncounterWithNonCodedDiagnosis() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_non_coded_diagnosis.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.code.coding",
                "There must be a Code from TR for Diagnosis", false, response);
    }

    @Test
    public void shouldValidateProcedureRequestWithSpecimen() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_procedure_request_for_lab_with_specimen.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldInvalidateProcedureRequestWithInvalidItem() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_procedure_request_with_invalid_code.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.code.coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/referenceterms/Creatinine-4df1-438e-9d72-invalid], code[Creatinine-4df1-438e-9d72-invalid]"
                , false, response);
    }

    @Test
    public void shouldValidateFamilyMemberHistory() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_family_member_history.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldRejectInvalidRelationshipTypeInFamilyMemberHistory() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_family_member_history_relationship_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.relationship.coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Relationship-Type], code[INVALID]", true, response);
    }

    @Test
    public void shouldValidateMedicationRequestWithScheduledDateExtension() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_scheduled_date.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateMedicationRequestWithCustomDosageExtension() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_custom_dosage.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosticReport() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_diagnostic_report.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosisWithPreviousDiagnosisExtension() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_updated_diagnosis.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldInvalidateDignosisWhenClinicalStatusIsNotGiven() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_updated_diagnosis_without_clinical_status.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertFailureInResponse("Bundle.entry[3].resource",
                "Condition.clinicalStatus SHALL be present if verificationStatus is not entered-in-error [verificationStatus='entered-in-error' or clinicalStatus.exists()]",
                false, response);
    }

    @Test
    public void shouldValidateDiagnosisWithPreviousProcedureRequestExtension() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_suspended_procedure_request.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_obs_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldInvalidateWrongCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_obs_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(2, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.code.coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/5dd8f02e-3cda-40f2-9c70-b1a9c91ff1da], code[5dd8f02e-3cda-40f2-9c70-b1a9c91ff1da-INVALID]",
                false, response);
        assertFailureInResponse("Bundle.entry[4].resource.related.target.resource.code.coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/5dd8f02e-3cda-40f2-9c70-b1a9c91ff1da], code[5dd8f02e-3cda-40f2-9c70-b1a9c91ff1da-INVALID]",
                false, response);
    }

    @Test
    public void shouldValidateIfTheHealthIdInTheEncounterContentIsNotSameAsTheOneExpected() {
        encounterBundle.setHealthId("1111222233334444555");
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertThat(response.getErrors().size(), is(3));
        assertTrue(response.getErrors().get(0).getReason().contains("Patient's Health Id does not match."));
        assertTrue(response.getErrors().get(1).getReason().contains("Patient's Health Id does not match."));
        assertTrue(response.getErrors().get(2).getReason().contains("Patient's Health Id does not match."));
    }

    @Test
    public void shouldRejectIfEncounterTypeIsNotACoding() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_without_encounter_type_coding.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertThat(response.getErrors().size(), is(1));
        assertEquals("There must be an encounter type code from TR", response.getErrors().get(0).getReason());
    }

    @Test
    public void shouldInvalidateWrongEncounterType() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_having_invalid_encounter_type.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertThat(response.getErrors().size(), is(1));
        assertEquals("Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type], code[Invalid]", response.getErrors().get(0).getReason());
    }

    @Test
    public void shouldValidateMedicationPrescriptionWithInvalidMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_invalid_medication.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.medicationCodeableConcept.coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/drugs/23d7e743-75bd-4a25-8f34-bd849bd50394], code[23d7e743-75bd-4a25-8f34-bd849bd50394-INVALID]",
                false, response);
    }

    @Test
    public void shouldValidateRouteInMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Medication prescription pass through validation", response.isSuccessful());
    }

    @Test
    public void shouldValidateDispenseMedicationInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Medication-prescription,Prescriber pass through validation", response.isSuccessful());
    }

    @Test
    public void shouldValidateSiteAndReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateDispenseAndAdditionalInstructionsInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Should Validate Valid Encounter In MedicationPrescription", response.isSuccessful());
    }

    @Test
    public void shouldValidateSubstitutionReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }


    @Test
    public void shouldValidateMethodAndAsNeededXInMedicationPrescription() {

        /**
         * p98001046534_encounter_with_medication_request_valid.xml has
         * 2 medication prescribed with asNeeded (boolean true), and asNeeded with CodeableConcept
         *
         */
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());

    }

    @Test
    public void shouldValidateInvalidDosageQuantityInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_medication_request_with_invalid_dose_quantity.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[3].resource.dosageInstruction.dose",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Quantity-Units123], code[TU]",
                false, response);
    }

    @Test
    public void shouldValidateDischargeSummaryEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_discharge_summury.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateInvalidCodeInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_discharge_summury_code_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(2, response.getErrors().size());
        assertFailureInResponse("Bundle.entry[4].resource.code.coding",
                "Could not validate concept system[http://172.18.46.53:9080/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca], code[07952dc2-5206-11e5-ae6d-0050568225ca]",
                false, response);
        assertFailureInResponse("Bundle.entry[7].resource.related[2].target.resource.code.coding",
                "Could not validate concept system[http://172.18.46.53:9080/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca], code[07952dc2-5206-11e5-ae6d-0050568225ca]",
                false, response);
    }

    @Test
    public void shouldValidateProcedure() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_procedure.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateInvalidEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_all_resources_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertEquals(38, response.getErrors().size());
    }

    @Test
    public void shouldValidateEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/stu3/p98001046534_encounter_with_all_resources.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

}
