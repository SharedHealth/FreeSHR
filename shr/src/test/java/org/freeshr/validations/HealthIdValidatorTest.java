package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.application.fhir.FhirMessageFilter;
import org.freeshr.utils.FileUtil;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HealthIdValidatorTest {

    private HealthIdValidator healthIdValidator;
    ResourceOrFeedDeserializer resourceOrFeedDeserializer;
    FhirMessageFilter fhirMessageFilter;

    @Before
    public void setup() {
        healthIdValidator = new HealthIdValidator();
        resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
        fhirMessageFilter = new FhirMessageFilter();
    }

    @Test
    public void shouldAcceptEncounterIfHealthIdInTheXmlMatchesTheGivenHealthId() {
        final String xml = FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        List<ValidationMessage> response = healthIdValidator.validate(getEncounterContext(xml, "5893922485019082753"));
        assertThat(EncounterValidationResponse.fromValidationMessages(response, fhirMessageFilter).isSuccessful(),
                is(true));
    }


    @Test
    public void shouldNotAcceptEncounterIfNoHealthIdIsPresentInComposition() {
        String xml = FileUtil.asString("xmls/encounters/invalid_composition.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        EncounterValidationResponse response = EncounterValidationResponse.fromValidationMessages(
                healthIdValidator.validate(getEncounterContext(xml, "5893922485019082753")), fhirMessageFilter);
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0).getReason(), is("Composition must have patient's Health Id in subject" +
                "."));
    }

    @Test
    public void shouldRejectEncounterIfHealthIdInTheXmlDoesNotMatchTheGivenHealthId() {
        String xml = FileUtil.asString("xmls/encounters/encounter.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        EncounterValidationResponse response = EncounterValidationResponse.fromValidationMessages(
                healthIdValidator.validate(getEncounterContext(xml, "11112222233333")), fhirMessageFilter);
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is(ResourceValidator.INVALID));
        assertThat(response.getErrors().get(0).getReason(), is("Patient's Health Id does not match."));
    }

    @Test
    public void shouldRejectEncounterIfThereIsNoHealthIdInTheComposition() {
        String xml = FileUtil.asString("xmls/encounters/encounter.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        EncounterValidationResponse response = EncounterValidationResponse.fromValidationMessages(
                healthIdValidator.validate(getEncounterContext(xml, "11112222233333")), fhirMessageFilter);
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is(ResourceValidator.INVALID));
        assertThat(response.getErrors().get(0).getReason(), is("Patient's Health Id does not match."));
    }


    private ValidationSubject<EncounterValidationContext> getEncounterContext(final String xml, final String healthId) {
        return new ValidationSubject<EncounterValidationContext>() {
            @Override
            public EncounterValidationContext extract() {
                EncounterBundle encounterBundle = new EncounterBundle();
                encounterBundle.setEncounterContent(xml);
                encounterBundle.setHealthId(healthId);
                return new EncounterValidationContext(encounterBundle, resourceOrFeedDeserializer);

            }
        };
    }

}