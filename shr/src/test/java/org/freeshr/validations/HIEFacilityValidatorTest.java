package org.freeshr.validations;

import org.freeshr.config.SHRProperties;
import org.freeshr.domain.service.FacilityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import rx.Observable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class HIEFacilityValidatorTest {

    @Mock
    SHRProperties shrProperties;

    @Mock
    FacilityService facilityService;

    HIEFacilityValidator validator;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        validator = new HIEFacilityValidator(shrProperties, facilityService);

    }

    @Test
    public void shouldNotValidateEmptyFacilityUrl() throws Exception {
        assertFalse(validator.validate(""));
    }

    @Test
    public void shouldNotValidateForInvalidFacilityUrlPattern() throws Exception {
        when(shrProperties.getFacilityReferencePath()).thenReturn("http://hie.org/facilities");

        assertFalse(validator.validate("some random string"));
    }

    @Test
    public void shouldNotValidateFacilityIfNotPresentOnHRM() throws Exception {
        when(shrProperties.getFacilityReferencePath()).thenReturn("http://hie.org/facilities");
        Mockito.when(facilityService.checkForFacility("invalid-facility-id")).thenReturn(Observable.just(false));

        assertFalse(validator.validate("http://hie.org/facilities/invalid-facility-id.json"));

    }

    @Test
    public void shouldValidateFacilityIfPresentOnHRM() throws Exception {
        when(shrProperties.getFacilityReferencePath()).thenReturn("http://hie.org/facilities");
        Mockito.when(facilityService.checkForFacility("valid-facility-id")).thenReturn(Observable.just(true));

        assertTrue(validator.validate("http://hie.org/facilities/valid-facility-id.json"));

    }
}