package org.freeshr.web.converter;

import org.apache.commons.io.FileUtils;
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
        final URL resource = URLClassLoader.getSystemResource("encounter.json");
        when(inputMessage.getBody()).thenReturn(resource.openStream());
        EncounterBundle bundle = new EncounterBundleMessageConverter().createEncounterBundle(inputMessage);

        Assert.assertNull(bundle.getEncounterId());
        Assert.assertEquals("patient-id-1000", bundle.getHealthId());
        Assert.assertEquals("2012-01-04T09:10:14Z", bundle.getDate());
        Assert.assertEquals(FileUtils.readFileToString(new File(resource.getPath())), bundle.getContent());
    }
}