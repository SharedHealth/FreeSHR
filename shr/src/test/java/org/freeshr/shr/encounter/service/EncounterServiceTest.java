package org.freeshr.shr.encounter.service;

import org.freeshr.shr.encounter.model.Encounter;
import org.freeshr.shr.encounter.repository.AllEncounters;
import org.freeshr.shr.encounter.service.EncounterService;
import org.freeshr.shr.patient.service.PatientRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterServiceTest {

    @Mock
    private AllEncounters allEncounters;
    @Mock
    private PatientRegistry patientRegistry;

    private EncounterService encounterService;

    @Before
    public void setup() {
        initMocks(this);
        encounterService = new EncounterService(allEncounters, patientRegistry);
    }

    @Test
    public void shouldCreateEncounterWhenHealthIdIsValid() {
        String healthId = "healthId";

        Encounter encounter = new Encounter();
        encounter.setHealthId(healthId);

        when(patientRegistry.isValid(healthId)).thenReturn(Boolean.TRUE);
        encounterService.ensureCreated(encounter);
        verify(allEncounters).save(encounter);
    }

    @Test
    public void shouldNotCreateEncounterWhenHealthIdIsNotValid() {
        String healthId = "healthId";

        Encounter encounter = new Encounter();
        encounter.setHealthId(healthId);

        when(patientRegistry.isValid(healthId)).thenReturn(Boolean.FALSE);
        encounterService.ensureCreated(encounter);
        verify(allEncounters, never()).save(encounter);
    }
}