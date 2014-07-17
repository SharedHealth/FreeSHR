package org.freeshr.interfaces.encounter.ws;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpInputMessage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncounterBundleMessageConverterTest {

    @Test
    public void shouldCreateEncounterFromHttpInputMessage() throws IOException {

        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        final URL resource = URLClassLoader.getSystemResource("encounters.json");
        when(inputMessage.getBody()).thenReturn(resource.openStream());
        EncounterBundle bundle = new EncounterBundleMessageConverter().createEncounterBundle(inputMessage);

        Assert.assertNull(bundle.getEncounterId());
        Assert.assertNull(bundle.getHealthId());
        Assert.assertNull(bundle.getDate());
        String expectedContent = StringUtils.deleteWhitespace(FileUtils.readFileToString(new File(resource.getPath())).replaceAll("\\n", ""));
        Assert.assertEquals(expectedContent, bundle.getContent().toString());
    }
}