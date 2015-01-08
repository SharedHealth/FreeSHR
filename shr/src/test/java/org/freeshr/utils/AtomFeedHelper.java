package org.freeshr.utils;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.data.EncounterBundleData;
import org.freeshr.validations.EncounterValidationContext;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Resource;

public class AtomFeedHelper {

    public static  ValidationSubject<AtomEntry<? extends Resource>> getAtomFeed(String feedFile) {
        EncounterBundle encounterBundle = EncounterBundleData.encounter(EncounterBundleData.HEALTH_ID,
                FileUtil.asString(feedFile));
        final EncounterValidationContext validationContext = new EncounterValidationContext(encounterBundle,
                new ResourceOrFeedDeserializer());
        return atomEntryFragment(validationContext.feedFragment().extract().getEntryList().get(2));
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
