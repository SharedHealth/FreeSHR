package org.freeshr.utils;

import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.formats.XmlParser;

import java.io.ByteArrayInputStream;

public class ResourceOrFeedDeserializer  {

    public ParserBase.ResourceOrFeed deserialize(String xml) {
        try {
            return new XmlParser(true).parseGeneral(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
