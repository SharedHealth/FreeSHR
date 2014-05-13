package org.freeshr.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeshr.shr.encounter.model.Encounter;
import org.freeshr.shr.encounter.service.EncounterService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.Charset;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(post("/encounter").content(content).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        verify(encounterService).ensureCreated(encounter);
    }
}