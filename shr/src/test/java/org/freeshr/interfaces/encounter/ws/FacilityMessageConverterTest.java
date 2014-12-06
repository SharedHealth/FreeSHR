package org.freeshr.interfaces.encounter.ws;

import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpInputMessage;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FacilityMessageConverterTest {

    private HttpInputMessage inputMessage;

    @Before
    public void setup() {
        inputMessage = mock(HttpInputMessage.class);
    }

    private URL mockMessageToReturn(String resourceName) throws IOException {
        final URL resource = URLClassLoader.getSystemResource(resourceName);
        when(inputMessage.getBody()).thenReturn(resource.openStream());
        return resource;
    }

    @Test
    public void shouldCreateFacilityFromHttpMessage() throws IOException {
        mockMessageToReturn("jsons/Facility.json");
        Facility facility = new FacilityMessageConverter().createFacility(inputMessage);
        assertNotNull(facility);
        assertEquals("Dhaka Divisional Health Office", facility.getFacilityName());
        assertEquals(1, facility.getCatchments().size());
        assertEquals("10000001", facility.getFacilityId());
        assertEquals("Divisional Level Office", facility.getFacilityType());
        assertEquals(new Address("30", "26", "01", "", ""), facility.getFacilityLocation());


    }
}
