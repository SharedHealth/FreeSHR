package org.freeshr.patient.service;

import org.freeshr.patient.model.Patient;
import org.freeshr.patient.repository.AllPatients;
import org.freeshr.patient.wrapper.MasterClientIndexWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
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

        when(allPatients.find(healthId)).thenReturn(new Patient());
        Boolean result = patientRegistry.isValid(healthId);
        assertTrue(result);
    }

    @Test
    public void shouldBeValidPatientWhenPatientCouldBeFoundInTheIndex() {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(null);
        when(masterClientIndexWrapper.isValid(healthId)).thenReturn(Boolean.TRUE);

        Boolean result = patientRegistry.isValid(healthId);
        assertTrue(result);
    }

    @Test
    public void shouldNotBeValidPatientWhenPatientCouldNotBeFound() {
        String healthId = "healthId";

        when(allPatients.find(healthId)).thenReturn(null);
        when(masterClientIndexWrapper.isValid(healthId)).thenReturn(Boolean.FALSE);

        Boolean result = patientRegistry.isValid(healthId);
        assertFalse(result);
    }
}
