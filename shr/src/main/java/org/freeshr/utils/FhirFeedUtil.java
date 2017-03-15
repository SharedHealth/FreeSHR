package org.freeshr.utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.LenientErrorHandler;
import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.stereotype.Component;

@Component
public class FhirFeedUtil {
    public static String FHIR_SCHEMA_VERSION = "v3";

    //TODO initialize when needed.
    private FhirContext fhirContext = initializeFhirContext();

    private FhirContext initializeFhirContext() {
        FhirContext fhirContext = FhirContext.forDstu3();
        fhirContext.setParserErrorHandler(new LenientErrorHandler().setErrorOnInvalidValue(false));
        return fhirContext;
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
