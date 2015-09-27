package org.freeshr.validations.bundle;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.service.FacilityService;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.utils.FileUtil;
import org.freeshr.validations.HIEFacilityValidator;
import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.ValidationSubject;
import org.freeshr.validations.bundle.FacilityValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import rx.Observable;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityValidatorTest {

    @Mock
    SHRProperties shrProperties;
    @Mock
    FacilityService facilityService;

    HIEFacilityValidator hieFacilityValidator;
    private FacilityValidator facilityValidator;
    private FhirFeedUtil fhirFeedUtil;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        fhirFeedUtil = new FhirFeedUtil();
        hieFacilityValidator = new HIEFacilityValidator(shrProperties, facilityService);
        facilityValidator = new FacilityValidator(hieFacilityValidator);
    }

    @Test
    public void shouldValidateFacilityReference() throws Exception {
        final String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml");
        when(shrProperties.getFacilityReferencePath()).thenReturn("http://172.18.46.199:8080/api/1.0/facilities");
        Mockito.when(facilityService.checkForFacility("10019841")).thenReturn(Observable.<Facility>just(new Facility()));
        List<ShrValidationMessage> response = facilityValidator.validate(getBundleFragment(xml));
        assertThat(EncounterValidationResponse.fromShrValidationMessages(response).isSuccessful(), is(true));
    }

    @Test
    public void shouldFailForInvalidFacilityRegistryReference() {
        final String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml");
        when(shrProperties.getFacilityReferencePath()).thenReturn("http://172.18.46.199:8080/api/1.0/facilities");
        Mockito.when(facilityService.checkForFacility("10019841")).thenReturn(Observable.<Facility>just(null));
        List<ShrValidationMessage> response = facilityValidator.validate(getBundleFragment(xml));
        assertThat(EncounterValidationResponse.fromShrValidationMessages(response).isSuccessful(), is(false));
    }

    @Test
    public void shouldFailForNonMatchingFacilityReference() {
        final String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses.xml");
        when(shrProperties.getFacilityReferencePath()).thenReturn("http://example.org/api/1.0/facilities");
        Mockito.when(facilityService.checkForFacility("10019841")).thenReturn(Observable.<Facility>just(null));
        List<ShrValidationMessage> response = facilityValidator.validate(getBundleFragment(xml));
        assertThat(EncounterValidationResponse.fromShrValidationMessages(response).isSuccessful(), is(false));
    }

    @Test
    public void shouldNotFailForMissingEncounterFacilityReference() {
        final String xml = FileUtil.asString("xmls/encounters/dstu2/p98001046534_encounter_without_serviceProvider.xml");
        when(shrProperties.getFacilityReferencePath()).thenReturn("http://example.org/api/1.0/facilities");
        Mockito.when(facilityService.checkForFacility("10019841")).thenReturn(Observable.<Facility>just(null));
        List<ShrValidationMessage> response = facilityValidator.validate(getBundleFragment(xml));
        assertEquals(1, response.size());
        Assert.assertEquals(Severity.INFORMATION, response.get(0).getSeverity());
        assertThat(EncounterValidationResponse.fromShrValidationMessages(response).isSuccessful(), is(true));
    }

    private ValidationSubject<Bundle> getBundleFragment(final String xml) {
        return new ValidationSubject<Bundle>() {
            @Override
            public Bundle extract() {
                return fhirFeedUtil.parseBundle(xml,"xml");
            }
        };
    }
}