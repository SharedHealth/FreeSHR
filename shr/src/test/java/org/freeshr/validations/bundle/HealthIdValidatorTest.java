package org.freeshr.validations.bundle;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.FileUtil;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.FhirMessageFilter;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.ValidationSubject;
import org.freeshr.validations.bundle.HealthIdValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class HealthIdValidatorTest {

    private HealthIdValidator healthIdValidator;
    FhirFeedUtil fhirFeedUtil;
    FhirMessageFilter fhirMessageFilter;

    @Mock
    SHRProperties shrProperties;

    @Before
    public void setup() {
        initMocks(this);

        healthIdValidator = new HealthIdValidator(shrProperties);
        fhirFeedUtil = new FhirFeedUtil();
        fhirMessageFilter = new FhirMessageFilter();
    }

    @Test
    public void shouldAcceptEncounterIfHealthIdInTheXmlMatchesTheGivenHealthId() {
        final String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_all_resources.xml");
        when(shrProperties.getPatientReferencePath()).thenReturn("http://localhost:9997/api/default/patients");
        List<ShrValidationMessage> response = healthIdValidator.validate(getEncounterContext(xml, "98001046534"));
        assertThat(EncounterValidationResponse.fromShrValidationMessages(response).isSuccessful(),
                is(true));
    }


    @Test
    public void shouldAcceptEncounterIfHealthIdInTheXmlMatchesTheGivenHealthIdAllVersions() {
        final String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml");
        when(shrProperties.getPatientReferencePath()).thenReturn("http://172.18.46.199:8081/api/default/patients");
        List<ShrValidationMessage> response = healthIdValidator.validate(getEncounterContext(xml, "98001046534"));
        assertThat(EncounterValidationResponse.fromShrValidationMessages(response).isSuccessful(),
                is(true));
    }

    @Test
    public void shouldNotAcceptEncounterIfNoHealthIdIsPresentInComposition() {
        //NOTE this is not actually needed as the check would be done at the XSD cardinality level. A composition without subject ref is not valid
        String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_without_composition_subject.xml");
        when(shrProperties.getPatientReferencePath()).thenReturn("http://172.18.46.199:8081/api/default/patients");
        EncounterValidationResponse response = EncounterValidationResponse.fromShrValidationMessages(
                healthIdValidator.validate(getEncounterContext(xml, "98001046534")));
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0).getReason(), is("Composition:Composition must have patient's Health Id in subject."));
    }

    @Test
    public void shouldRejectEncounterIfHealthIdInTheXmlDoesNotMatchTheGivenHealthId() {
        final String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml");
        when(shrProperties.getPatientReferencePath()).thenReturn("http://172.18.46.199:8081/api/default/patients");
        EncounterValidationResponse response = EncounterValidationResponse.fromShrValidationMessages(
                healthIdValidator.validate(getEncounterContext(xml, "11112222233333")));
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is("invalid"));
        assertTrue("Didn't respond with proper message", response.getErrors().get(0).getReason().endsWith("Patient's Health Id does not match."));
    }

    @Test
    public void shouldRejectEncounterIfThereIsNoHealthIdInTheComposition() {
        String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_invalid_condition_patient.xml");
        when(shrProperties.getPatientReferencePath()).thenReturn("http://172.18.46.199:8081/api/default/patients");
        EncounterValidationResponse response = EncounterValidationResponse.fromShrValidationMessages(
                healthIdValidator.validate(getEncounterContext(xml, "98001046534")));
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is("invalid"));
        assertEquals("f:Condition/f:patient", response.getErrors().get(0).getField());
        assertTrue("Didn't respond with proper message", response.getErrors().get(0).getReason().endsWith("Patient's Health Id does not match."));
    }

    private ValidationSubject<EncounterValidationContext> getEncounterContext(final String xml, final String healthId) {
        return new ValidationSubject<EncounterValidationContext>() {
            @Override
            public EncounterValidationContext extract() {
                EncounterBundle encounterBundle = new EncounterBundle();
                encounterBundle.setEncounterContent(xml);
                encounterBundle.setHealthId(healthId);
                return new EncounterValidationContext(encounterBundle, fhirFeedUtil);

            }
        };
    }


}