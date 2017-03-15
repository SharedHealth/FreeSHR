package org.freeshr.utils;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class BundleHelper {
    public static Bundle parseBundle(String content, FhirContext context) {
        return (Bundle) parseResource(content, context);
    }

    public static IBaseResource parseResource(String content, FhirContext context) {
        return context.newXmlParser().parseResource(content);
    }
}
