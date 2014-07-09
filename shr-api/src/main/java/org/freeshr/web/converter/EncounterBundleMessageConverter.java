package org.freeshr.web.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.web.dto.Bundle;
import org.freeshr.web.dto.Composition;
import org.hl7.fhir.instance.model.Encounter;
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
        return MediaType.APPLICATION_JSON.equals(mediaType);
    }

    @Override
    protected EncounterBundle readInternal(Class<? extends EncounterBundle> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return createEncounterBundle(inputMessage);
    }

    EncounterBundle createEncounterBundle(HttpInputMessage inputMessage) throws IOException {
        EncounterBundle encounterBundle = new EncounterBundle();
        String messageBody = IOUtils.toString(inputMessage.getBody());

        Bundle bundle = objectMapper.readValue(messageBody, Bundle.class);
        Composition composition = bundle.getEntries().get(0).getContent();
        Encounter encounter = composition.getSections().get(0);

        encounterBundle.setHealthId(encounter.getSubject().getReference().getValue());
        encounterBundle.setDate(composition.getDate());
        encounterBundle.setContent(messageBody);
        return encounterBundle;
    }

    @Override
    protected void writeInternal(EncounterBundle encounter, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    }
}
