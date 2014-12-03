package org.freeshr.infrastructure.tr;


import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class ValueSetCodeValidatorIntegrationTest {
    @Autowired
    private SHRProperties shrProperties;

    @Test
    public void shouldCheckIfUrlShouldBeCreated() {
        ValueSetCodeValidator validator = new ValueSetCodeValidator(null, null);
        assertFalse(validator.shouldCreateUrl("http://example.com"));
        assertFalse(validator.shouldCreateUrl("https://example.com"));
        assertTrue(validator.shouldCreateUrl("encounter-type"));
    }

    @Test
    public void shouldFormatURL() {
        ValueSetCodeValidator validator = new ValueSetCodeValidator(null, shrProperties);

        String url = validator.formatUrl("encounter-type");
        String expectedURL = "http://192.168.33.10:9080/openmrs/ws/rest/v1/tr/vs/encounter-type";
        assertEquals(expectedURL, url);
    }

}