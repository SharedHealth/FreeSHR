package org.freeshr.utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
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

    private FhirContext fhirContext = FhirContext.forDstu2();

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

    public Bundle parseBundle(String content, String type) {
        if (type.equals("xml")) {
            return (Bundle) fhirContext.newXmlParser().parseResource(content);
        } else {
            return (Bundle) fhirContext.newJsonParser().parseResource(content);
        }
    }

    public String encodeBundle(Bundle bundle, String type) {
        if (type.equals("xml")) {
            return fhirContext.newXmlParser().encodeResourceToString(bundle);
        } else {
            return fhirContext.newJsonParser().encodeResourceToString(bundle);
        }
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }
}
