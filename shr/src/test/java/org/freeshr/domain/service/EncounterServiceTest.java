package org.freeshr.domain.service;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.ResourceOrFeedDeserializer;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.EncounterValidator;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserInfo.HRM_FACILITY_ADMIN_GROUP;
import static org.freeshr.infrastructure.security.UserInfo.SHR_USER_GROUP;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncounterServiceTest {

    private FacilityService mockFacilityService;
    private EncounterService encounterService;
    private EncounterRepository mockEncounterRepository;
    private EncounterValidator mockEncounterValidator;
    private PatientService mockPatientService;

    @Before
    public void setup() {
        mockEncounterRepository = mock(EncounterRepository.class);
        mockFacilityService = mock(FacilityService.class);
        mockEncounterValidator = mock(EncounterValidator.class);
        mockPatientService = mock(PatientService.class);
        encounterService = new EncounterService(mockEncounterRepository, mockPatientService,
                mockEncounterValidator);
    }


    @Test
    public void shouldReturnErrorEvenIfOneGetEncounterFails() throws ParseException {
        Date date = new SimpleDateFormat("dd/mm/YYYY").parse("10/9/2014");
        when(mockFacilityService.ensurePresent("1")).thenReturn(
                Observable.just(new Facility("1", "facility1", "Main hospital", "3056,30", new Address("1", "2", "3",
                        null, null))));

        final String exceptionMessage = "I bombed";

        when(mockEncounterRepository.findEncountersForCatchment(eq(new Catchment("30")),
                any(Date.class), eq(20))).
                thenReturn(Observable.<List<EncounterBundle>>error(new Exception(exceptionMessage)));
        try {
            encounterService.findEncountersForFacilityCatchment(
                    "30", date, 20).toBlocking().first();
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals(e.getCause().getMessage(), exceptionMessage);
        }
    }

    @Test
    public void shouldPopulateEncounterBundleFields() throws Exception {
        String facilityId = "10000069";
        String healthId = "10001";
        UserInfo userInfo = getUserInfo(facilityId);
        Patient confidentialPatient = new Patient();
        confidentialPatient.setHealthId(healthId);
        confidentialPatient.setConfidentiality(true);
        EncounterBundle bundle = createEncounterBundle(healthId);

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new ResourceOrFeedDeserializer().deserialize(bundle.getContent()));
        when(mockEncounterValidator.validate(any(EncounterValidationContext.class))).thenReturn(encounterValidationResponse);
        when(mockPatientService.ensurePresent(any(String.class), eq(userInfo))).thenReturn(Observable.just(confidentialPatient));
        when(mockEncounterRepository.save(bundle, confidentialPatient)).thenReturn(Observable.just(true));

        encounterService.ensureCreated(bundle, userInfo).toBlocking().first();

        verify(mockEncounterRepository).save(bundle, confidentialPatient);
        assertEquals(Confidentiality.VeryRestricted, bundle.getPatientConfidentiality());
        assertEquals(Confidentiality.Normal, bundle.getEncounterConfidentiality());
        assertNotNull(bundle.getEncounterId());
        assertNotNull(bundle.getReceivedDate());
        assertNotNull(bundle.getUpdatedDate());
        assertEquals(facilityId, bundle.getCreatedBy().getFacilityId());
        assertNull(bundle.getCreatedBy().getProviderId());
        assertEquals(facilityId, bundle.getUpdatedBy().getFacilityId());
        assertNull(bundle.getUpdatedBy().getProviderId());
    }

    private EncounterBundle createEncounterBundle(String healthId) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setHealthId(healthId);
        bundle.setEncounterContent(asString("xmls/encounters/valid_encounter_with_patient_mocked.xml"));
        return bundle;
    }

    private UserInfo getUserInfo(String facilityId) {
        return new UserInfo("102", "foo", "email@gmail.com", 1, true,
                "xyz", new ArrayList<>(asList(SHR_USER_GROUP, HRM_FACILITY_ADMIN_GROUP)), asList(new UserProfile("facility", facilityId, asList("3026"))));
    }
}
