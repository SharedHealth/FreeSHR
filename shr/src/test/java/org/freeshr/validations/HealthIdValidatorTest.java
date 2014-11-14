package org.freeshr.validations;

import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class HealthIdValidatorTest {

    private HealthIdValidator healthIdValidator;

    @Before
    public void setup(){
        healthIdValidator = new HealthIdValidator();
    }

    @Test
    public void shouldAcceptEncounterIfHealthIdInTheXmlMatchesTheGivenHealthId() {
        EncounterValidationResponse response = healthIdValidator.validate(FileUtil.asString("xmls/encounters/diagnostic_order_valid.xml"), "5893922485019082753");
        assertThat(response.isSuccessful(), is(true));
    }

    @Test
    public void shouldRejectEncounterIfHealthIdInTheXmlDoesNotMatchTheGivenHealthId(){
        EncounterValidationResponse response = healthIdValidator.validate(FileUtil.asString("xmls/encounters/encounter.xml"), "11112222233333");
        assertThat(response.isSuccessful(), is(false));
        assertThat(response.getErrors().get(0).getType(), is(ResourceValidator.INVALID));
        assertThat(response.getErrors().get(0).getReason(), is("Patient's Health Id does not match."));
    }

}