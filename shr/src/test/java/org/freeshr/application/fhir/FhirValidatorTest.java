package org.freeshr.application.fhir;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class FhirValidatorTest {

    private FhirValidator validator;

    @Mock
    private TRConceptLocator trConceptLocator;

    @Autowired
    private SHRProperties shrProperties;

    EncounterBundle encounterBundle;

    @Before
    public void setup() {
        initMocks(this);

        validator = new FhirValidator(trConceptLocator, shrProperties);
        encounterBundle = EncounterBundleData.withValidEncounter("health-id");
    }

    @Test
    public void shouldValidateEncounterWhenInProperFormat() throws Exception {
        encounterBundle = EncounterBundleData.encounter("healthId", FileUtil.asString("xmls/encounters/encounter.xml"));
        assertTrue(validator.validate(encounterBundle.getEncounterContent().toString()));
    }

    @Test
    public void shouldRejectEncounterWithInvalidConcept() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("invalid-eddb01eb-61fc-4f9e-aca5"), anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error, "Invalid code"));

        encounterBundle = EncounterBundleData.encounter("healthId", FileUtil.asString("xmls/encounters/invalid_concept.xml"));
        assertFalse(validator.validate(encounterBundle.getEncounterContent().toString()));
    }

    @Test
    public void shouldRejectEncounterWithInvalidConceptReferenceTerms() {
        when(trConceptLocator.verifiesSystem(anyString())).thenReturn(true);
        when(trConceptLocator.validate(anyString(), eq("INVALID_REFERENCE_TERM"), anyString())).thenReturn(new ConceptLocator.ValidationResult(OperationOutcome.IssueSeverity.error, "Invalid code"));

        encounterBundle = EncounterBundleData.encounter("healthId", FileUtil.asString("xmls/encounters/invalid_ref.xml"));
        assertFalse(validator.validate(encounterBundle.getEncounterContent().toString()));
    }

    @Test
    public void shouldRejectEncounterWithMissingSystemForDiagnosis() throws Exception {
        encounterBundle = EncounterBundleData.encounter("healthId", FileUtil.asString("xmls/encounters/diagnosis_system_missing.xml"));
        assertFalse(validator.validate(encounterBundle.getEncounterContent().toString()));
    }

    @Test
    public void shouldRejectEncountersWithDiagnosisHavingAllInvalidSystems() {
        encounterBundle = EncounterBundleData.encounter("healthId", FileUtil.asString("xmls/encounters/diagnosis_system_invalid.xml"));
        assertFalse(validator.validate(encounterBundle.getEncounterContent().toString()));
    }

    @Test
    public void shouldRejectInvalidDiagnosisCategory() {
        encounterBundle = EncounterBundleData.encounter("healthId", FileUtil.asString("xmls/encounters/diagnosis_category_invalid.xml"));
        assertFalse(validator.validate(encounterBundle.getEncounterContent().toString()));
    }
}
