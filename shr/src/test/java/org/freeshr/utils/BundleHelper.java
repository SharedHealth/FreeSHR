package org.freeshr.utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;

public class BundleHelper {

    public static ValidationSubject<Bundle.BundleEntryComponent> getBundle(String feedFile, ResourceType resourceType) {
        EncounterBundle encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString(feedFile));
        final EncounterValidationContext validationContext = new EncounterValidationContext(encounterBundle,
                new FhirFeedUtil());

        Bundle feed = validationContext.feedFragment().extract();
        List<Bundle.BundleEntryComponent> feedEntryList = feed.getEntry();
        for (Bundle.BundleEntryComponent atomEntry : feedEntryList) {
            if (atomEntry.getResource().getResourceType().equals(resourceType)) {
                return atomEntryFragment(atomEntry);
            }
        }
        return null;
    }

    private static ValidationSubject<Bundle.BundleEntryComponent>
    atomEntryFragment(final Bundle.BundleEntryComponent atomEntry) {
        return new ValidationSubject<Bundle.BundleEntryComponent>() {
            @Override
            public Bundle.BundleEntryComponent extract() {
                return atomEntry;
            }
        };
    }

    public static ca.uhn.fhir.model.dstu2.resource.Bundle parseBundle(String content, FhirContext context) {
        return (ca.uhn.fhir.model.dstu2.resource.Bundle) parseResource(content, context);
    }

    public static IBaseResource parseResource(String content, FhirContext context) {
        return context.newXmlParser().parseResource(content);
    }
}
