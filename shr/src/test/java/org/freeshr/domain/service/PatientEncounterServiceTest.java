package org.freeshr.domain.service;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterValidationResponse;
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
import org.mockito.ArgumentCaptor;
import rx.Observable;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserInfo.HRM_FACILITY_ADMIN_GROUP;
import static org.freeshr.infrastructure.security.UserInfo.SHR_USER_GROUP;
import static org.freeshr.utils.FileUtil.asString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class PatientEncounterServiceTest {

    private PatientEncounterService patientEncounterService;
    private EncounterRepository mockEncounterRepository;
    private EncounterValidator mockEncounterValidator;
    private PatientService mockPatientService;

    @Before
    public void setup() {
        mockEncounterRepository = mock(EncounterRepository.class);
        mockEncounterValidator = mock(EncounterValidator.class);
        mockPatientService = mock(PatientService.class);
        patientEncounterService = new PatientEncounterService(mockEncounterRepository, mockPatientService,
                mockEncounterValidator);
    }


    @Test
    public void shouldPopulateEncounterBundleFieldsOnEncounterCreate() throws Exception {
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

        patientEncounterService.ensureCreated(bundle, userInfo).toBlocking().first();

        verify(mockEncounterRepository).save(bundle, confidentialPatient);
        assertEquals(Confidentiality.VeryRestricted, bundle.getPatientConfidentiality());
        assertEquals(Confidentiality.Normal, bundle.getEncounterConfidentiality());
        assertNotNull(bundle.getEncounterId());
        assertNotNull(bundle.getReceivedAt());
        assertNotNull(bundle.getUpdatedAt());
        assertEquals(facilityId, bundle.getCreatedBy().getFacilityId());
        assertNull(bundle.getCreatedBy().getProviderId());
        assertEquals(facilityId, bundle.getUpdatedBy().getFacilityId());
        assertNull(bundle.getUpdatedBy().getProviderId());
    }

    @Test
    public void shouldPopulateEncounterBundleFieldsOnEncounterUpdate() throws Exception {
        String facilityId = "10000069";
        String healthId = "10001";
        UserInfo userInfo = getUserInfo(facilityId);
        Patient confidentialPatient = new Patient();
        confidentialPatient.setHealthId(healthId);
        confidentialPatient.setConfidentiality(true);
        EncounterBundle bundle = createEncounterBundle(healthId);
        String encounterId = "encounter_id1";
        bundle.setEncounterId(encounterId);
        EncounterBundle existingEncounterBundle = createEncounterBundle(healthId);
        existingEncounterBundle.setEncounterId(encounterId);

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new ResourceOrFeedDeserializer().deserialize(bundle.getContent()));
        when(mockEncounterValidator.validate(any(EncounterValidationContext.class))).thenReturn(encounterValidationResponse);
        when(mockPatientService.ensurePresent(eq(healthId), eq(userInfo))).thenReturn(Observable.just(confidentialPatient));
        when(mockEncounterRepository.findEncounterById(encounterId)).thenReturn(Observable.just(existingEncounterBundle));
        when(mockEncounterRepository.updateEncounter(bundle, existingEncounterBundle, confidentialPatient)).thenReturn(Observable.just(true));

        patientEncounterService.ensureUpdated(bundle, userInfo).toBlocking().first();

        ArgumentCaptor<EncounterBundle> updatedEncounterBundleCapture = ArgumentCaptor.forClass(EncounterBundle.class);
        verify(mockEncounterRepository).updateEncounter(updatedEncounterBundleCapture.capture(), eq(bundle), eq(confidentialPatient));

        EncounterBundle updatedEncounterBundle = updatedEncounterBundleCapture.getValue();

        assertEquals(Confidentiality.Normal, updatedEncounterBundle.getEncounterConfidentiality());
        assertNotNull(updatedEncounterBundle.getEncounterId());
        assertNotNull(updatedEncounterBundle.getUpdatedAt());
        assertEquals(facilityId, updatedEncounterBundle.getUpdatedBy().getFacilityId());
        assertNull(updatedEncounterBundle.getUpdatedBy().getProviderId());
        assertEquals(2, updatedEncounterBundle.getContentVersion());
        assertEquals(bundle.getReceivedAt(), updatedEncounterBundle.getReceivedAt());
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
