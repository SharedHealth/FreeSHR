package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.utils.FileUtil;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.hl7.fhir.instance.model.AtomFeed;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HealthIdValidatorTest {

    private HealthIdValidator healthIdValidator;
    ResourceOrFeedDeserializer resourceOrFeedDeserializer;

    @Before
    public void setup(){
        healthIdValidator = new HealthIdValidator();
        resourceOrFeedDeserializer = new ResourceOrFeedDeserializer();
    }

    @Test
    public void shouldAcceptEncounterIfHealthIdInTheXmlMatchesTheGivenHealthId() {
        String xml = FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        EncounterValidationResponse response = healthIdValidator.validate(feed, "5893922485019082753");
        assertThat(response.isSuccessful(), is(true));
    }

    @Test
    public void shouldNotAcceptEncounterIfNoHealthIdIsPresentInComposition() {
        String xml = FileUtil.asString("xmls/encounters/invalid_composition.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        EncounterValidationResponse response = healthIdValidator.validate(feed, "5893922485019082753");
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0).getReason(), is("Composition must have patient's Health Id in subject."));
    }

    @Test
    public void shouldRejectEncounterIfHealthIdInTheXmlDoesNotMatchTheGivenHealthId(){
        String xml = FileUtil.asString("xmls/encounters/encounter.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        EncounterValidationResponse response = healthIdValidator.validate(feed, "11112222233333");
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is(ResourceValidator.INVALID));
        assertThat(response.getErrors().get(0).getReason(), is("Patient's Health Id does not match."));
    }

    @Test
    public void shouldRejectEncounterIfThereIsNoHealthIdInTheComposition(){
        String xml = FileUtil.asString("xmls/encounters/encounter.xml");
        AtomFeed feed = resourceOrFeedDeserializer.deserialize(xml);
        EncounterValidationResponse response = healthIdValidator.validate(feed, "11112222233333");
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is(ResourceValidator.INVALID));
        assertThat(response.getErrors().get(0).getReason(), is("Patient's Health Id does not match."));
    }

}