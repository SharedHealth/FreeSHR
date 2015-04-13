package org.freeshr.interfaces.encounter.ws;

import org.apache.commons.io.FileUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpInputMessage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncounterBundleMessageConverterTest {

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
    public void shouldCreateEncounterFromHttpInputMessage() throws IOException {
        final URL resource = mockMessageToReturn("xmls/encounters/encounter.xml");
        EncounterBundle bundle = new EncounterBundleMessageConverter().createEncounterBundle(inputMessage);

        Assert.assertNull(bundle.getEncounterId());
        Assert.assertNull(bundle.getHealthId());
        Assert.assertNull(bundle.getReceivedAt());

        String expectedContent = FileUtils.readFileToString(new File(resource.getPath()));
        String actualContent = bundle.getEncounterContent().toString();
        Assert.assertEquals(expectedContent, actualContent);
    }
}