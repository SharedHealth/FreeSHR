package org.freeshr.domain.service;

import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MCIClient;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import rx.Observable;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MCIClient mciClient;

    private PatientService patientService;

    @Before
    public void setup() {
        initMocks(this);
        patientService = new PatientService(patientRepository, mciClient);
    }

    @Test
    public void shouldQueryAllPatientsToVerifyValidity() throws ExecutionException, InterruptedException {
        String healthId = "healthId";
        String clientId = "123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        when(patientRepository.find(healthId)).thenReturn(Observable.just(new Patient()));
        assertNotNull(patientService.ensurePresent(healthId, getUserInfo(clientId, email, securityToken)).toBlocking().first());

        verify(patientRepository).find(healthId);
    }

    @Test
    public void shouldNotQueryMasterClientIndexEagerly() throws ExecutionException, InterruptedException {
        String healthId = "healthId";
        String clientId = "123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        when(patientRepository.find(healthId)).thenReturn(Observable.just(new Patient()));
        patientService.ensurePresent(healthId, getUserInfo(clientId, email, securityToken));

        verify(mciClient, never()).getPatient(healthId, getUserInfo(clientId, email, securityToken));
    }

    @Test
    public void shouldReturnTrueWhenPatientIsNotFoundLocallyButFoundInTheClientIndex() throws ExecutionException,
            InterruptedException {
        String healthId = "healthId";
        String clientId = "123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        when(patientRepository.find(healthId)).thenReturn(Observable.<Patient>just(null));
        Patient somePatient = new Patient();
        UserInfo userInfo = getUserInfo(clientId, email, securityToken);
        when(mciClient.getPatient(healthId, userInfo)).thenReturn(Observable.just(somePatient));

        assertNotNull(patientService.ensurePresent(healthId, userInfo).toBlocking().first());
        verify(patientRepository).save(somePatient);
    }

    @Test
    public void shouldReturnNullWhenPatientIsNotFoundEitherLocallyOrInTheClientIndex() throws ExecutionException,
            InterruptedException {
        String healthId = "healthId";
        String clientId = "123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        when(patientRepository.find(healthId)).thenReturn(Observable.<Patient>just(null));
        UserInfo userInfo = getUserInfo(clientId, email, securityToken);
        when(mciClient.getPatient(healthId, userInfo)).thenReturn(Observable.<Patient>just(null));

        assertTrue(null == patientService.ensurePresent(healthId, userInfo).toBlocking().first());
    }

    @Test
    public void shouldNotConsiderActivePatientAsMerged() throws Exception {
        Patient patient = new Patient();
        patient.setActive(true);
        Observable<Patient> patientObservable = Observable.from(asList(patient));
        when(patientRepository.find("active patient")).thenReturn(patientObservable);

        assertNull(patientService.getPatientMergedWith("active patient"));
    }

    @Test
    public void shouldCheckIfPatientIsMerged() throws Exception {
        Patient patient = new Patient();
        patient.setActive(false);
        patient.setMergedWith("Some other patient");
        Observable<Patient> patientObservable = Observable.from(asList(patient));
        when(patientRepository.find("merged patient")).thenReturn(patientObservable);

        assertEquals("Some other patient",patientService.getPatientMergedWith("merged patient"));

    }

    private UserInfo getUserInfo(String clientId, String email, String securityToken) {
        return new UserInfo(clientId, "foo", email, 1, true,
                securityToken, new ArrayList<String>(), asList(new UserProfile("facility", "10000069", asList("3026"))));
    }
}