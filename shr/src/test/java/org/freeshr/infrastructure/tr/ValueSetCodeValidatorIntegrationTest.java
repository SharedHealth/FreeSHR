package org.freeshr.infrastructure.tr;


import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.config.SHRProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = SHRConfig.class)
public class ValueSetCodeValidatorIntegrationTest {

    @Autowired
    ValueSetCodeValidator validator;

    @Test
    public void shouldCheckIfUrlShouldBeCreated() {
        assertFalse(validator.shouldCreateUrl("http://example.com"));
        assertFalse(validator.shouldCreateUrl("https://example.com"));
        assertTrue(validator.shouldCreateUrl("encounter-type"));
    }

    @Test
    public void shouldFormatURL() {
        String url = validator.formatUrl("encounter-type");
        //locahost:9997 is because test-shr.properties has TR_SERVER_BASE_URL accordingly
        String expectedURL = "http://localhost:9997/openmrs/ws/rest/v1/tr/vs/encounter-type";
        assertEquals(expectedURL, url);
    }


    @Test
    public void shouldRejectInvalidValueSetCode() {
        Observable<Boolean> observable = validator.isValid("http://random.org" + ValueSetCodeValidator.VALUE_SET_PATTERN + "encounter-type", "REG");
        assertFalse("Should have failed for invalid valueset reference URL", observable.toBlocking().first());
    }

}