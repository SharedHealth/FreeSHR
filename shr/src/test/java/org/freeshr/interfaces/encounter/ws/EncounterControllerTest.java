package org.freeshr.interfaces.encounter.ws;

import com.google.common.base.Charsets;
import org.freeshr.config.SHRConfig;
import org.freeshr.config.SHREnvironmentMock;
import org.freeshr.launch.WebMvcConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.freeshr.utils.FileUtil.asString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = SHREnvironmentMock.class, classes = {WebMvcConfig.class, SHRConfig.class})
@WebAppConfiguration
public class EncounterControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldSaveEncounter() throws Exception {
        mockMvc.perform(post("/patients/hid-1/encounters")
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .characterEncoding(Charsets.UTF_8.name())
                .content(asString("xmls/encounters/encounter.xml")))
                .andExpect(status().isOk());

    }
}