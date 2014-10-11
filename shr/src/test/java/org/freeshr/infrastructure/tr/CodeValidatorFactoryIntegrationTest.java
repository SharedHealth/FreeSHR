package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class CodeValidatorFactoryIntegrationTest {

    @Autowired
    private CodeValidatorFactory factory;

    @Test
    public void shouldIdentifyTheCorrectValidator() throws Exception {
        assertTrue(factory.getValidator("http://tr.shr.com/openmrs/ws/rest/v1/tr/referenceterms/ref1") instanceof HttpCodeValidator);
        assertTrue(factory.getValidator("http://tr.shr.com/openmrs/ws/rest/v1/tr/concepts/ref1") instanceof HttpCodeValidator);
    }
}