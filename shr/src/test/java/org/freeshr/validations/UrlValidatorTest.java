package org.freeshr.validations;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlValidatorTest{

    @Test
    public void shouldPassAValidUrl() throws Exception {
        UrlValidator urlValidator = new UrlValidator();
        assertTrue(urlValidator.isValid("http://some.valid.url"));

    }

    @Test
    public void shouldRejectAnInValidUrl() throws Exception {
        UrlValidator urlValidator = new UrlValidator();
        assertFalse(urlValidator.isValid("some-junk"));

    }
}