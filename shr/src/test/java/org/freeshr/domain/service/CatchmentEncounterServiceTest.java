package org.freeshr.domain.service;


import org.freeshr.domain.model.Catchment;
import org.freeshr.events.EncounterEvent;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CatchmentEncounterServiceTest {

    private CatchmentEncounterService catchmentEncounterService;
    private EncounterRepository mockEncounterRepository;

    @Before
    public void setup() {
        mockEncounterRepository = mock(EncounterRepository.class);
        catchmentEncounterService = new CatchmentEncounterService(mockEncounterRepository
        );
    }


    @Test
    public void shouldReturnErrorEvenIfOneGetEncounterFails() throws ParseException {
        Date date = new SimpleDateFormat("dd/mm/YYYY").parse("10/9/2014");
        final String exceptionMessage = "I bombed";

        when(mockEncounterRepository.findEncounterFeedForCatchment(eq(new Catchment("30")),
                any(Date.class), eq(20))).
                thenReturn(Observable.<List<EncounterEvent>>error(new Exception(exceptionMessage)));
        try {
            catchmentEncounterService.findEncounterFeedForFacilityCatchment(
                    "30", date, 20).toBlocking().first();
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals(e.getCause().getMessage(), exceptionMessage);
        }
    }
}
