package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class Hl7CodeValidatorTest {

    @Autowired
    Hl7CodeValidator hl7CodeValidator;

    @Test
    public void shouldValidateHl7Code() throws Exception {
        assertTrue(hl7CodeValidator.isValid("http://hl7.org/fhir/vs/animal-breeds", "gsd").get());
        assertTrue(hl7CodeValidator.isValid("http://hl7.org/fhir/vs/animal-breeds", "tibmas").get());
        assertFalse(hl7CodeValidator.isValid("http://hl7.org/fhir/vs/animal-breeds", "invalid-code").get());
        assertFalse(hl7CodeValidator.isValid("http://hl7.org/fhir/vs/invalid-url", "tibmas").get());
        assertFalse(hl7CodeValidator.isValid(null, null).get());
    }

}