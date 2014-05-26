package org.freeshr.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.freeshr.domain.model.encounter.Encounter;
import org.freeshr.domain.service.EncounterService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EncounterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EncounterService encounterService;

    private EncounterController encounterController;

    @Before
    public void setup() {
        initMocks(this);
        encounterController = new EncounterController(encounterService);
        mockMvc = MockMvcBuilders.standaloneSetup(encounterController).build();
    }

    @Test
    public void shouldRespondWithOkWhenCreatingEncounter() throws Exception {
        String healthId = "healthId";
        Encounter encounter = new Encounter();
        encounter.setHealthId(healthId);

        String content = new ObjectMapper().writeValueAsString(encounter);

        when(encounterService.ensureCreated(encounter)).thenReturn(new PreResolvedListenableFuture<Boolean>(Boolean.TRUE));
        mockMvc.perform
                (
                        post("/encounter").content(content).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(Boolean.TRUE));
        verify(encounterService).ensureCreated(encounter);
    }

    @Test
    public void shouldRespondWithErrorWhenUnableToResolvePromisedValue() throws Exception {
        String healthId = "healthId";
        Encounter encounter = new Encounter();
        encounter.setHealthId(healthId);

        String content = new ObjectMapper().writeValueAsString(encounter);

        when(encounterService.ensureCreated(encounter)).thenReturn(failure);
        mockMvc.perform
                (
                        post("/encounter").content(content).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(request().asyncResult(instanceOf(RuntimeException.class)));
    }

    private static ListenableFuture<Boolean> failure = new ListenableFuture<Boolean>() {

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Boolean get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public void addCallback(ListenableFutureCallback<? super Boolean> callback) {
            callback.onFailure(new RuntimeException("Exception"));
        }
    };
}