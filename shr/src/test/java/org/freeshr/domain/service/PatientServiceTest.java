package org.freeshr.domain.service;

import org.freeshr.domain.model.patient.Patient;
import org.freeshr.infrastructure.mci.MasterClientIndexClient;
import org.freeshr.infrastructure.persistence.PatientRepository;
import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientServiceTest {

    @Mock
    private PatientRepository allPatients;
    @Mock
    private MasterClientIndexClient masterClientIndexClient;

    private PatientService patientService;

    @Before
    public void setup() {
        initMocks(this);
        patientService = new PatientService(allPatients, masterClientIndexClient);
    }

    @Test
    public void shouldQueryAllPatientsToVerifyValidity() throws ExecutionException, InterruptedException {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(new Patient());
        assertNotNull(patientService.ensurePresent(healthId));
        verify(allPatients).find(healthId);
    }

    @Test
    public void shouldNotQueryMasterClientIndexEagerly() throws ExecutionException, InterruptedException {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(new Patient());
        patientService.ensurePresent(healthId);
        verify(masterClientIndexClient, never()).getPatient(healthId);
    }

    @Test
    public void shouldReturnTrueWhenPatientIsNotFoundLocallyButFoundInTheClientIndex() throws ExecutionException, InterruptedException {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(null);
        when(masterClientIndexClient.getPatient(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(new Patient()));
        assertNotNull(patientService.ensurePresent(healthId));
    }

    @Test
    public void shouldReturnNullWhenPatientIsNotFoundEitherLocallyOrInTheClientIndex() throws ExecutionException, InterruptedException {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(null);
        when(masterClientIndexClient.getPatient(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(null));
        assertTrue(null == patientService.ensurePresent(healthId));
    }


}