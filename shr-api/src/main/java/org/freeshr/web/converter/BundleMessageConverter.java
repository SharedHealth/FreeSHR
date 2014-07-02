package org.freeshr.web.converter;

import org.apache.commons.io.IOUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

public class BundleMessageConverter extends AbstractHttpMessageConverter<EncounterBundle> {

    @Override
    protected boolean supports(Class<?> clazz) {
        return EncounterBundle.class.equals(clazz);
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.equals(mediaType);
    }

    @Override
    protected EncounterBundle readInternal(Class<? extends EncounterBundle> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        EncounterBundle bundle = new EncounterBundle();
        String content = IOUtils.toString(inputMessage.getBody());
        bundle.setContent(content);
        return bundle;
    }

    @Override
    protected void writeInternal(EncounterBundle encounter, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    }
}
