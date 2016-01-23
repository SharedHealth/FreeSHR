package org.freeshr.application.fhir;

import ca.uhn.fhir.context.FhirContext;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import net.sf.ehcache.CacheManager;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.infrastructure.tr.ValueSetCodeValidator;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.HapiEncounterValidator;
import org.freeshr.validations.bundle.FacilityValidator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.freeshr.util.ValidationFailureTestHelper.assertFailureFromResponseErrors;
import static org.freeshr.util.ValidationFailureTestHelper.assertFailureInResponse;
import static org.freeshr.utils.FileUtil.asString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
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
    @Autowired
    private HapiEncounterValidator validator;

    private TRConceptValidator trConceptValidator;

    private EncounterValidationContext validationContext;
    EncounterBundle encounterBundle;

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

        //encounter-type
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/vs/encounter-type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/encounter-type-case-insensitive.json"))));


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
                        .withBody(asString("jsons/trValueset_medication_order_reason.json"))));

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


    }

    @After
    public void tearDown() throws Exception {
        CacheManager.getInstance().clearAll();
    }

    @Test
    public void shouldValidateEncounterIfItHasAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldFailIfNotAValidFacility() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_invalid_facility.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        String expectedErrorReason = FacilityValidator.INVALID_SERVICE_PROVIDER + ":http://127.0.0.1:9997/facilities/100000603.json";
        assertFailureInResponse("Encounter", expectedErrorReason, false, response);
    }

    @Test
    public void shouldFailIfFacilityUrlIsInvalid() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_invalid_facility_url.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("Encounter", FacilityValidator.INVALID_SERVICE_PROVIDER, true,
                response);
        assertEquals(1, response.getErrors().size());
    }

    /**
     * Dependency: StructureValidator
     *
     * @throws Exception
     */
    @Test
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
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca], code[INVALID-07952dc2-5206-11e5-ae6d-0050568225ca]",
                false, response);
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptReferenceTerms() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_localRefs_with_invalidRefTerm.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding[1]",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/referenceterms/2f6z9872-4df1-438e-9d72-0a8b161d409b], code[INVALID-A90]",
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
                "The value provided is not in the value set http://hl7.org/fhir/ValueSet/condition-clinical (http://hl7.org/fhir/ValueSet/condition-clinical, and a code is recommended to come from this value set", true, response);
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
        assertEquals(6, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:code/f:coding/f:system",
                "@value cannot be empty", false, response);
        assertFailureInResponse("/f:Bundle/f:entry[3]/f:resource/f:Condition/f:code/f:coding/f:system",
                "@value cannot be empty", false, response);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:category",
                "None of the codes provided are in the value set http://hl7.org/fhir/ValueSet/condition-category (http://hl7.org/fhir/ValueSet/condition-category, and a code is recommended to come from this value set", true, response);
        assertFailureInResponse("/f:Bundle/f:entry[3]/f:resource/f:Condition/f:category",
                "None of the codes provided are in the value set http://hl7.org/fhir/ValueSet/condition-category (http://hl7.org/fhir/ValueSet/condition-category, and a code is recommended to come from this value set", true, response);
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:Condition/f:clinicalStatus",
                "The value provided is not in the value set http://hl7.org/fhir/ValueSet/condition-clinical (http://hl7.org/fhir/ValueSet/condition-clinical, and a code is recommended to come from this value set", true, response);
        assertFailureInResponse("/f:Bundle/f:entry[3]/f:resource/f:Condition/f:clinicalStatus",
                "The value provided is not in the value set http://hl7.org/fhir/ValueSet/condition-clinical (http://hl7.org/fhir/ValueSet/condition-clinical, and a code is recommended to come from this value set", true, response);
    }

    @Test
    public void shouldValidateDiagnosticOrderWithSpecimen() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_diagnostic_order_with_specimen.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldInvalidateDiagnosticOrderWithInvalidItem() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_diagnostic_order_with_invalid_item.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:DiagnosticOrder/f:item/f:code/f:coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/referenceterms/Creatinine-4df1-438e-9d72-invalid], code[Creatinine-4df1-438e-9d72-invalid]"
                , false, response);
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
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:FamilyMemberHistory/f:relationship/f:coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Relationship-Type], code[INVALID]", true, response);
    }

    @Test
    public void shouldValidateMedicationOrderWithScheduledDateExtension() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_scheduled_date.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateMedicationOrderWithCustomDosageExtension() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_custom_dosage.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosticReport() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnostic_report.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateDiagnosisWithPreviousDiagnosisExtension() throws Exception {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_updated_diagnosis.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }


    @Test
    @Ignore("Ignoring because currently instance validator skips if there in no system and code")
    public void shouldValidateConditionsToCheckIfCategoriesOtherThanChiefComplaintAreCoded() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_coded_and_nocoded_diagnosis.xml"));
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
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_obs_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldInvalidateWrongCodesInObservations() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_obs_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry[3]/f:resource/f:Observation/f:code/f:coding",
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
    public void shouldValidateEncounterTypeAgainstValueSet() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_valid_type.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateMedicationPrescriptionWithInvalidMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_invalid_medication.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:MedicationOrder/f:medicationCodeableConcept/f:coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/drugs/23d7e743-75bd-4a25-8f34-bd849bd50394], code[23d7e743-75bd-4a25-8f34-bd849bd50394-INVALID]",
                false, response);
    }

    @Test
    public void shouldValidateRouteInMedicationReference() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Medication prescription pass through validation", response.isSuccessful());
    }

    @Test
    @Ignore("Ignored because currently we are skipping if Dispense has a medication reference")
    public void shouldValidateDispenseMedicationInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Medication-prescription,Prescriber pass through validation", response.isSuccessful());
    }

    @Test
    public void shouldValidateSiteAndReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateDispenseAndAdditionalInstructionsInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue("Should Validate Valid Encounter In MedicationPrescription", response.isSuccessful());
    }

    @Test
    public void shouldValidateInvalidDispenseInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_with_invalid_dispense.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry/f:resource/f:MedicationOrder/f:dispenseRequest/f:medicationCodeableConcept/f:coding",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/drugs/23d7e743-75bd-4a25-8f34-bd849bd50394], code[23d7e743-75bd-4a25-8f34-bd849bd50394-INVALID]",
                false, response);
    }

    @Test
    public void shouldValidateSubstitutionTypeAndReasonInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }


    @Test
    public void shouldValidateMethodAndAsNeededXInMedicationPrescription() {

        /**
         * p98001046534_encounter_with_medication_order_valid.xml has
         * 2 medication prescribed with asNeeded (boolean true), and asNeeded with CodeableConcept
         *
         */
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_valid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());

    }

    @Test
    public void shouldValidateInvalidDosageQuantityInMedicationPrescription() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_medication_order_with_invalid_dose_quantity.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("f:MedicationOrder/f:dosageInstruction/f:dose",
                "Could not validate concept system[http://localhost:9997/openmrs/ws/rest/v1/tr/vs/Quantity-Units123], code[TU]",
                false, response);
    }

    @Test
    public void shouldValidateDischargeSummaryEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_discharge_summury.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    @Ignore("This test is actually for discharge summary, but testing condition schema")
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
    @Ignore("Testing Medication instead of Discharge Summary")
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
    @Ignore("Testing Medication instead of Discharge Summary")
    public void shouldValidateInvalidDosageQuantityInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu1/discharge_summary_dosage_quantity_invalid.xml"));
        when(trConceptValidator.isCodeSystemSupported(any(FhirContext.class), anyString())).thenReturn(true);
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFailureFromResponseErrors("urn:5fc6d0d9-9520-4015-87cb-ab0cfa7e4b50", "Invalid Dosage Quantity",
                response.getErrors());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void shouldValidateInvalidCodeInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_discharge_summury_code_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry[4]/f:resource/f:Observation/f:code/f:coding",
                "Could not validate concept system[http://172.18.46.53:9080/openmrs/ws/rest/v1/tr/concepts/07952dc2-5206-11e5-ae6d-0050568225ca], code[07952dc2-5206-11e5-ae6d-0050568225ca]",
                false, response);
    }

    @Test
    public void shouldValidateMissingSystemCodeInDischargeSummaryEncounter() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_discharge_summury_system_missing.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertEquals(1, response.getErrors().size());
        assertFailureInResponse("/f:Bundle/f:entry[4]/f:resource/f:Observation/f:code/f:coding/f:system",
                "@value cannot be empty", false, response);
    }

    @Test
    public void shouldValidateProcedure() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_procedure.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldValidateInvalidEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_all_resources_invalid.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertFalse(response.isSuccessful());
        assertEquals(30, response.getErrors().size());
    }

    @Test
    public void shouldValidateEncounterWithAllResources() {
        encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_all_resources.xml"));
        validationContext = new EncounterValidationContext(encounterBundle, new FhirFeedUtil());
        EncounterValidationResponse response = validator.validate(validationContext);
        assertTrue(response.isSuccessful());
    }

}
