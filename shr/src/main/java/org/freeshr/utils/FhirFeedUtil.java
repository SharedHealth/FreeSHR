package org.freeshr.utils;

import org.hl7.fhir.instance.formats.XmlComposer;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class FhirFeedUtil {

    public AtomFeed deserialize(String xml) {
        try {
            return new XmlParser(true).parseGeneral(new ByteArrayInputStream(xml.getBytes())).getFeed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String serialize(AtomFeed feed) {
        XmlComposer composer = new XmlComposer();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            composer.compose(byteArrayOutputStream, feed, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toString();
    }

    public AtomEntry<? extends Resource> getAtomEntryOfResourceType(List<AtomEntry<? extends Resource>> entryList, ResourceType resourceType) {
        for (AtomEntry<? extends Resource> atomEntry : entryList) {
            Resource resource = atomEntry.getResource();
            if (resource.getResourceType().equals(resourceType)) {
                return atomEntry;
            }
        }
        return null;
    }
}
