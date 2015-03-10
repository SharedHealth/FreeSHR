package org.freeshr.domain.service;

import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MCIClient;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import rx.Observable;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        when(patientRepository.find(healthId)).thenReturn(Observable.just(new Patient()));
        assertNotNull(patientService.ensurePresent(healthId, clientId, email, securityToken).toBlocking().first());
        verify(patientRepository).find(healthId);
    }

    @Test
    public void shouldNotQueryMasterClientIndexEagerly() throws ExecutionException, InterruptedException {
        String healthId = "healthId";
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        when(patientRepository.find(healthId)).thenReturn(Observable.just(new Patient()));
        patientService.ensurePresent(healthId, clientId, email, securityToken);
        verify(mciClient, never()).getPatient(healthId, clientId, email, securityToken);
    }

    @Test
    public void shouldReturnTrueWhenPatientIsNotFoundLocallyButFoundInTheClientIndex() throws ExecutionException,
            InterruptedException {
        String healthId = "healthId";
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();
        
        when(patientRepository.find(healthId)).thenReturn(Observable.<Patient>just(null));
        Patient somePatient = new Patient();
        when(mciClient.getPatient(healthId, clientId, email, securityToken)).thenReturn(Observable.just(somePatient));
        assertNotNull(patientService.ensurePresent(healthId, clientId, email, securityToken).toBlocking().first());
        verify(patientRepository).save(somePatient);
    }

    @Test
    public void shouldReturnNullWhenPatientIsNotFoundEitherLocallyOrInTheClientIndex() throws ExecutionException,
            InterruptedException {
        String healthId = "healthId";
        String clientId="123";
        String email = "email@gmail.com";
        String securityToken = UUID.randomUUID().toString();

        when(patientRepository.find(healthId)).thenReturn(Observable.<Patient>just(null));
        when(mciClient.getPatient(healthId, clientId, email, securityToken)).thenReturn(Observable.<Patient>just(null));
        assertTrue(null == patientService.ensurePresent(healthId, clientId, email, securityToken).toBlocking().first());
    }


}