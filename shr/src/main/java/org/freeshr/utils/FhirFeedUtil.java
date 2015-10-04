package org.freeshr.utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FhirFeedUtil {

    //TODO initialize when needed.
    private FhirContext fhirContext = FhirContext.forDstu2();

    public org.hl7.fhir.instance.model.Bundle deSerialize(String xml) {
        try {
//            return new XmlParser(true).parseGeneral(new ByteArrayInputStream(xml.getBytes())).getFeed();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String serialize(org.hl7.fhir.instance.model.Bundle feed) {
//        XmlComposer composer = new XmlComposer();
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try {
//            composer.compose(byteArrayOutputStream, feed, true);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return byteArrayOutputStream.toString();
        return null;
    }

    public org.hl7.fhir.instance.model.Bundle.BundleEntryComponent getAtomEntryOfResourceType(List<org.hl7.fhir.instance.model.Bundle.BundleEntryComponent> entryList, ResourceType resourceType) {
        for (org.hl7.fhir.instance.model.Bundle.BundleEntryComponent bundleEntryComponent : entryList) {
            Resource resource = bundleEntryComponent.getResource();
            if (resource.getResourceType().equals(resourceType)) {
                return bundleEntryComponent;
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

    public org.hl7.fhir.instance.model.api.IBaseResource parseResource(String content, String type) {
        if (type.equals("xml")) {
            return fhirContext.newXmlParser().parseResource(content);
        } else {
            return fhirContext.newJsonParser().parseResource(content);
        }
    }
}
