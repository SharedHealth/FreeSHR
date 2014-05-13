package org.freeshr.shr.patient.service;

import org.freeshr.shr.patient.model.Patient;
import org.freeshr.shr.patient.repository.AllPatients;
import org.freeshr.shr.patient.service.PatientRegistry;
import org.freeshr.shr.patient.wrapper.MasterClientIndexWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    public void shouldBeValidPatientWhenPatientCouldBeFoundLocally() {
        String healthId = "healthId";

        Mockito.when(allPatients.find(healthId)).thenReturn(new Patient());
        Boolean result = patientRegistry.isValid(healthId);
        assertTrue(result);
    }

    @Test
    public void shouldBeValidPatientWhenPatientCouldBeFoundInTheIndex() {
        String healthId = "healthId";

        Mockito.when(allPatients.find(healthId)).thenReturn(null);
        Mockito.when(masterClientIndexWrapper.isValid(healthId)).thenReturn(Boolean.TRUE);

        Boolean result = patientRegistry.isValid(healthId);
        assertTrue(result);
    }

    @Test
    public void shouldNotBeValidPatientWhenPatientCouldNotBeFound() {
        String healthId = "healthId";

        Mockito.when(allPatients.find(healthId)).thenReturn(null);
        Mockito.when(masterClientIndexWrapper.isValid(healthId)).thenReturn(Boolean.FALSE);

        Boolean result = patientRegistry.isValid(healthId);
        assertFalse(result);
    }
}
