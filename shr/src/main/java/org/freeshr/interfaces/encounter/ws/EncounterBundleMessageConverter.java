package org.freeshr.interfaces.encounter.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterContent;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

public class EncounterBundleMessageConverter extends AbstractHttpMessageConverter<EncounterBundle> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean supports(Class<?> clazz) {
        return EncounterBundle.class.equals(clazz);
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.getType().equals(mediaType.getType()) &&
                MediaType.APPLICATION_JSON.getSubtype().equals(mediaType.getSubtype()) &&
                Charsets.UTF_8.equals(mediaType.getCharSet());
    }

    @Override
    protected EncounterBundle readInternal(Class<? extends EncounterBundle> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return createEncounterBundle(inputMessage);
    }

    EncounterBundle createEncounterBundle(HttpInputMessage inputMessage) throws IOException {
        EncounterBundle encounterBundle = new EncounterBundle();
        encounterBundle.setEncounterContent(IOUtils.toString(inputMessage.getBody()));
        return encounterBundle;
    }

    @Override
    protected void writeInternal(EncounterBundle encounter, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    }
}
