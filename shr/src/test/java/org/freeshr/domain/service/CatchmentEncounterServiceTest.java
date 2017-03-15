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
import static org.mockito.Mockito.*;

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

        when(mockEncounterRepository.findEncounterFeedForCatchmentUpdatedSince(eq(new Catchment("30")),
                any(Date.class), eq(20))).
                thenReturn(Observable.<List<EncounterEvent>>error(new Exception(exceptionMessage)));
        try {
            catchmentEncounterService.findEncounterFeedForFacilityCatchment(
                    "30", date, null).toBlocking().first();
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals(e.getCause().getMessage(), exceptionMessage);
        }
    }

    @Test
    public void shouldFetchEncountersByCatchmentBasedOnLastMarkerIfPresent() throws Exception {
        Date date = new SimpleDateFormat("dd/mm/YYYY").parse("10/9/2014");
        catchmentEncounterService.findEncounterFeedForFacilityCatchment("3026", date, "last-read-marker");

        verify(mockEncounterRepository).findEncounterFeedForCatchmentAfterMarker(new Catchment("3026"), "last-read-marker", date, 20);
        verify(mockEncounterRepository, times(0)).findEncounterFeedForCatchmentUpdatedSince(new Catchment("3026"), date, 20);
    }

    @Test
    public void shouldFetchEncountersByCatchmentBasedOnUpdatedSinceIfLastMarkerIsAbsent() throws Exception {
        Date date = new SimpleDateFormat("dd/mm/YYYY").parse("10/9/2014");
        catchmentEncounterService.findEncounterFeedForFacilityCatchment("3026", date, null);

        verify(mockEncounterRepository).findEncounterFeedForCatchmentUpdatedSince(new Catchment("3026"), date, 20);
        verify(mockEncounterRepository, times(0)).findEncounterFeedForCatchmentAfterMarker(eq(new Catchment("3026")), anyString(), eq(date), eq(20));

    }
}
