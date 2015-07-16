package org.freeshr.utils;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.List;

public class AtomFeedHelper {

    public static ValidationSubject<AtomEntry<? extends Resource>> getAtomFeed(String feedFile, ResourceType resourceType) {
        EncounterBundle encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString(feedFile));
        final EncounterValidationContext validationContext = new EncounterValidationContext(encounterBundle,
                new FhirFeedUtil());

        AtomFeed feed = validationContext.feedFragment().extract();
        List<AtomEntry<? extends Resource>> feedEntryList = feed.getEntryList();
        for (AtomEntry<? extends Resource> atomEntry : feedEntryList) {
            if (atomEntry.getResource().getResourceType().equals(resourceType)) {
                return atomEntryFragment(atomEntry);
            }
        }
        return null;
    }

    private static ValidationSubject<AtomEntry<? extends Resource>>
    atomEntryFragment(final AtomEntry<? extends Resource> atomEntry) {
        return new ValidationSubject<AtomEntry<? extends Resource>>() {
            @Override
            public AtomEntry<? extends Resource> extract() {
                return atomEntry;
            }
        };
    }
}
