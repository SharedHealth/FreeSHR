package org.freeshr.shr.patient.service;

import org.freeshr.shr.concurrent.PreResolvedListenableFuture;
import org.freeshr.shr.patient.model.Patient;
import org.freeshr.shr.patient.repository.AllPatients;
import org.freeshr.shr.patient.wrapper.MasterClientIndexWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientRegistryTest {

    @Mock
    private AllPatients allPatients;
    @Mock
    private MasterClientIndexWrapper masterClientIndexWrapper;

    private PatientRegistry patientRegistry;

    @Before
    public void setup() {
        initMocks(this);
        patientRegistry = new PatientRegistry(allPatients, masterClientIndexWrapper);
    }

    @Test
    public void shouldQueryAllPatientsToVerifyValidity() throws ExecutionException, InterruptedException {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(new Patient()));
        assertTrue(patientRegistry.ensurePresent(healthId).get());
        verify(allPatients).find(healthId);
    }

    @Test
    public void shouldNotQueryMasterClientIndexEagerly() {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(new Patient()));
        patientRegistry.ensurePresent(healthId);
        verify(masterClientIndexWrapper, never()).getPatient(healthId);
    }

    @Test
    public void shouldReturnTrueWhenPatientIsNotFoundLocallyButFoundInTheClientIndex() throws ExecutionException, InterruptedException {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(null));
        when(masterClientIndexWrapper.getPatient(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(new Patient()));
        assertTrue(patientRegistry.ensurePresent(healthId).get());
    }

    @Test
    public void shouldReturnFalseWhenPatientIsNotFoundEitherLocallyOrInTheClientIndex() throws ExecutionException, InterruptedException {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(null));
        when(masterClientIndexWrapper.getPatient(healthId)).thenReturn(new PreResolvedListenableFuture<Patient>(null));
        assertFalse(patientRegistry.ensurePresent(healthId).get());
    }
}