package org.freeshr.web.converter;

import org.hl7.fhir.instance.model.Encounter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

public class BundleMessageConverter extends AbstractHttpMessageConverter<Encounter> {

    @Override
    protected boolean supports(Class<?> clazz) {
        return org.freeshr.domain.model.encounter.Encounter.class == clazz;
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.equals(mediaType);
    }

    @Override
    protected Encounter readInternal(Class<? extends Encounter> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(Encounter encounter, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    }
}
