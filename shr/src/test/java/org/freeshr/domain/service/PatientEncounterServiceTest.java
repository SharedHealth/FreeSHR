package org.freeshr.domain.service;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.application.fhir.EncounterValidationResponse;
import org.freeshr.domain.model.Requester;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.FhirFeedUtil;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.EncounterValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rx.Observable;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserInfo.*;
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
        UserInfo userInfo = getUserInfoFacility(facilityId);
        Patient confidentialPatient = new Patient();
        confidentialPatient.setHealthId(healthId);
        confidentialPatient.setConfidentiality(true);
        EncounterBundle bundle = createEncounterBundle(healthId, "encounterId");

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new FhirFeedUtil().deserialize(bundle.getContent()));
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
        UserInfo userInfo = getUserInfoFacility(facilityId);
        Patient confidentialPatient = new Patient();
        confidentialPatient.setHealthId(healthId);
        confidentialPatient.setConfidentiality(true);
        EncounterBundle bundle = createEncounterBundle(healthId, "encounter_id1");
        Requester createdBy = new Requester(facilityId, null);
        bundle.setUpdatedBy(createdBy);
        EncounterBundle existingEncounterBundle = createEncounterBundle(healthId, "encounter_id1");
        existingEncounterBundle.setCreatedBy(createdBy);

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new FhirFeedUtil().deserialize(bundle.getContent()));
        when(mockEncounterValidator.validate(any(EncounterValidationContext.class))).thenReturn(encounterValidationResponse);
        when(mockPatientService.ensurePresent(eq(healthId), eq(userInfo))).thenReturn(Observable.just(confidentialPatient));
        when(mockEncounterRepository.findEncounterById("encounter_id1")).thenReturn(Observable.just(existingEncounterBundle));
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

    @Test
    public void shouldNotEditEncounterIfUpdateRequestIsFromADifferentFacility() throws ExecutionException, InterruptedException {
        String facilityThatCreatedEncounter = "10000069";
        String facilityThatUpdatedEncounter = "10000070";
        String healthId = "10001";
        UserInfo userInfoOfFacilityThatUpdatedEncounter = getUserInfoFacility(facilityThatUpdatedEncounter);
        Patient patient = new Patient();
        EncounterBundle existingEncounterBundle = createEncounterBundle(healthId, "encounter_id1");
        existingEncounterBundle.setCreatedBy(new Requester(facilityThatCreatedEncounter, null));

        EncounterBundle bundle = createEncounterBundle(healthId, "encounter_id1");
        bundle.setUpdatedBy(new Requester(facilityThatUpdatedEncounter, null));

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new FhirFeedUtil().deserialize(bundle.getContent()));
        when(mockEncounterValidator.validate(any(EncounterValidationContext.class))).thenReturn(encounterValidationResponse);
        when(mockPatientService.ensurePresent(eq(healthId), eq(userInfoOfFacilityThatUpdatedEncounter))).thenReturn(Observable.just(patient));
        when(mockEncounterRepository.findEncounterById("encounter_id1")).thenReturn(Observable.just(existingEncounterBundle));

        EncounterResponse updateResponse = patientEncounterService.ensureUpdated(bundle, userInfoOfFacilityThatUpdatedEncounter).toBlocking().first();

        assertTrue(updateResponse.isTypeOfFailure(EncounterResponse.TypeOfFailure.Forbidden));
        verify(mockEncounterRepository,never()).updateEncounter(bundle, existingEncounterBundle, patient);

    }

    @Test
    public void shouldNotEditEncounterIfUpdateRequestIsFromADifferentProvider() throws ExecutionException, InterruptedException {
        String providerWhoCreatedEncounter = "1234";
        String providerWhoUpdatedEncounter = "7890";
        String healthId = "10001";
        UserInfo userInfoOfProviderWhoUpdatedEncounter = getUserInfoFacility(providerWhoUpdatedEncounter);
        Patient patient = new Patient();
        EncounterBundle existingEncounterBundle = createEncounterBundle(healthId, "encounter_id1");
        existingEncounterBundle.setCreatedBy(new Requester(providerWhoCreatedEncounter, null));

        EncounterBundle bundle = createEncounterBundle(healthId, "encounter_id1");
        bundle.setUpdatedBy(new Requester(providerWhoUpdatedEncounter, null));

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new FhirFeedUtil().deserialize(bundle.getContent()));
        when(mockEncounterValidator.validate(any(EncounterValidationContext.class))).thenReturn(encounterValidationResponse);
        when(mockPatientService.ensurePresent(eq(healthId), eq(userInfoOfProviderWhoUpdatedEncounter))).thenReturn(Observable.just(patient));
        when(mockEncounterRepository.findEncounterById("encounter_id1")).thenReturn(Observable.just(existingEncounterBundle));

        EncounterResponse updateResponse = patientEncounterService.ensureUpdated(bundle, userInfoOfProviderWhoUpdatedEncounter).toBlocking().first();

        assertTrue(updateResponse.isTypeOfFailure(EncounterResponse.TypeOfFailure.Forbidden));
        verify(mockEncounterRepository,never()).updateEncounter(bundle, existingEncounterBundle, patient);

    }

    @Test
    public void shouldEditEncounterIfUpdateRequestIsFromASameFacility() throws ExecutionException, InterruptedException {
        String facilityId = "10000069";
        String healthId = "10001";
        UserInfo userInfo = getUserInfoFacility(facilityId);
        Patient patient = new Patient();
        EncounterBundle existingEncounterBundle = createEncounterBundle(healthId, "encounter_id1");
        Requester facilityWhichCreatedEncounter = new Requester(facilityId, null);
        existingEncounterBundle.setCreatedBy(facilityWhichCreatedEncounter);

        EncounterBundle bundle = createEncounterBundle(healthId, "encounter_id1");
        bundle.setUpdatedBy(facilityWhichCreatedEncounter);

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new FhirFeedUtil().deserialize(bundle.getContent()));
        when(mockEncounterValidator.validate(any(EncounterValidationContext.class))).thenReturn(encounterValidationResponse);
        when(mockPatientService.ensurePresent(eq(healthId), eq(userInfo))).thenReturn(Observable.just(patient));
        when(mockEncounterRepository.findEncounterById("encounter_id1")).thenReturn(Observable.just(existingEncounterBundle));
        when(mockEncounterRepository.updateEncounter(bundle, existingEncounterBundle, patient)).thenReturn(Observable.just(true));

        patientEncounterService.ensureUpdated(bundle, userInfo).toBlocking().first();

        verify(mockEncounterRepository).updateEncounter(bundle, existingEncounterBundle, patient);

    }

    @Test
    public void shouldEditEncounterIfUpdateRequestIsFromASameProvider() throws ExecutionException, InterruptedException {
        String providerId = "10000069";
        String healthId = "10001";
        UserInfo userInfo = getUserInfoProvider(providerId);
        Patient patient = new Patient();
        EncounterBundle existingEncounterBundle = createEncounterBundle(healthId, "encounter_id1");
        Requester providerWhoCreatedEncounter = new Requester(null, providerId);
        existingEncounterBundle.setCreatedBy(providerWhoCreatedEncounter);

        EncounterBundle bundle = createEncounterBundle(healthId, "encounter_id1");
        bundle.setUpdatedBy(providerWhoCreatedEncounter);

        EncounterValidationResponse encounterValidationResponse = new EncounterValidationResponse();
        encounterValidationResponse.setFeed(new FhirFeedUtil().deserialize(bundle.getContent()));
        when(mockEncounterValidator.validate(any(EncounterValidationContext.class))).thenReturn(encounterValidationResponse);
        when(mockPatientService.ensurePresent(eq(healthId), eq(userInfo))).thenReturn(Observable.just(patient));
        when(mockEncounterRepository.findEncounterById("encounter_id1")).thenReturn(Observable.just(existingEncounterBundle));
        when(mockEncounterRepository.updateEncounter(bundle, existingEncounterBundle, patient)).thenReturn(Observable.just(true));

        patientEncounterService.ensureUpdated(bundle, userInfo).toBlocking().first();

        verify(mockEncounterRepository).updateEncounter(bundle, existingEncounterBundle, patient);

    }

    private EncounterBundle createEncounterBundle(String healthId, String encounterId) {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setHealthId(healthId);
        bundle.setEncounterId(encounterId);
        bundle.setEncounterContent(asString("xmls/encounters/valid_encounter_with_patient_mocked.xml"));
        return bundle;
    }

    private UserInfo getUserInfoFacility(String facilityId) {
        return new UserInfo("102", "foo", "email@gmail.com", 1, true,
                "xyz", new ArrayList<>(asList(SHR_USER_GROUP, HRM_FACILITY_ADMIN_GROUP)), asList(new UserProfile("facility", facilityId, asList("3026"))));
    }

    private UserInfo getUserInfoProvider(String providerId) {
        return new UserInfo("102", "provider-abc", "provider@gmail.com", 1, true,
                "xyz", new ArrayList<>(asList(SHR_USER_GROUP, HRM_PROVIDER_GROUP)), asList(new UserProfile("provider", providerId, asList("3026"))));
    }
}
