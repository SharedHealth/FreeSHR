package org.freeshr.domain.service;


import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.FhirValidator;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncounterServiceTest {

    FacilityRegistry facilityRegistry;
    EncounterService encounterService;
    EncounterRepository encounterRepository;

    @Before
    public void setup(){
        encounterRepository = mock(EncounterRepository.class);
        facilityRegistry = mock(FacilityRegistry.class);
        encounterService = new EncounterService(encounterRepository, mock(PatientRegistry.class), mock(FhirValidator.class), facilityRegistry);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnErrorEvenIfOneGetEncounterFails() throws ExecutionException, InterruptedException, ParseException {
        String date = "2014-09-10";
        ListenableFuture<Facility> facilityListenableFuture = (ListenableFuture<Facility>)mock(ListenableFuture.class);
        when(facilityListenableFuture.get()).thenReturn(new Facility("1", "facility1", "Main hospital", "3056,30", new Address("1", "2", "3", null, null)));
        when(facilityRegistry.ensurePresent("1")).thenReturn(facilityListenableFuture);

        final String exceptionMessage = "I bombed";
        when(encounterRepository.findAllEncountersByCatchment("9999", "district_id", date)).thenReturn(getMockListenableFuture(exceptionMessage));


        encounterService.findEncountersByCatchments("1", date).addCallback(new ListenableFutureCallback<List<EncounterBundle>>() {
            @Override
            public void onFailure(Throwable t) {
                assertTrue(t != null);
                assertEquals(exceptionMessage, t.getMessage());
            }

            @Override
            public void onSuccess(List<EncounterBundle> result) {
                assertTrue(result.size() == 0);
            }
        });


    }

    private ListenableFuture<List<EncounterBundle>> getMockListenableFuture(final String message) {
        return new ListenableFuture<List<EncounterBundle>>() {
            @Override
            public List<EncounterBundle> get() throws InterruptedException, ExecutionException {
                throw new RuntimeException(message);
            }

            @Override
            public List<EncounterBundle> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }

            @Override
            public void addCallback(ListenableFutureCallback<? super List<EncounterBundle>> callback) {

            }

            @Override
            public void addCallback(SuccessCallback<? super List<EncounterBundle>> successCallback, FailureCallback failureCallback) {

            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }


        };
    }
}
