package org.freeshr.domain.service;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.model.Catchment;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.freeshr.validations.EncounterValidator;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncounterServiceTest {

    FacilityService facilityService;
    EncounterService encounterService;
    EncounterRepository encounterRepository;

    @Before
    public void setup() {
        encounterRepository = mock(EncounterRepository.class);
        facilityService = mock(FacilityService.class);
        encounterService = new EncounterService(encounterRepository, mock(PatientService.class),
                mock(EncounterValidator.class), facilityService);
    }


    @Test
    public void shouldReturnErrorEvenIfOneGetEncounterFails() throws ParseException {
        Date date = new SimpleDateFormat("dd/mm/YYYY").parse("10/9/2014");
        when(facilityService.ensurePresent("1")).thenReturn(
                Observable.just(new Facility("1", "facility1", "Main hospital", "3056,30", new Address("1", "2", "3",
                        null, null))));

        final String exceptionMessage = "I bombed";

        when(encounterRepository.findEncountersForCatchment(eq(new Catchment("30")),
                org.mockito.Matchers.any(Date.class), eq(20))).
                thenReturn(Observable.<List<EncounterBundle>>error(new Exception(exceptionMessage)));
        try {
            List<EncounterBundle> encountersByCatchments = encounterService.findEncountersForFacilityCatchment("1",
                    "30", date, 20).toBlocking().first();
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals(e.getCause().getMessage(), exceptionMessage);
        }
    }
}
