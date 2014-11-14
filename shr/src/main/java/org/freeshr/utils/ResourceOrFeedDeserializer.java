package org.freeshr.utils;

import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.AtomFeed;

import java.io.ByteArrayInputStream;

public class ResourceOrFeedDeserializer  {

    public ResourceOrFeedDeserializer(){}

    public AtomFeed deserialize(String xml) {
        try {
            return new XmlParser(true).parseGeneral(new ByteArrayInputStream(xml.getBytes())).getFeed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
