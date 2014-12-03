package org.freeshr.validations;

import org.hl7.fhir.instance.model.*;

import java.util.List;

public abstract class AtomEntryValidator implements Validator<AtomEntry<? extends Resource>> {
    protected static final String CODEABLE_CONCEPT = "CodeableConcept";
    protected boolean bothSystemAndCodePresent(Property property) {
        boolean bothSystemAndCodePresent = false;
        List<Coding> codings = ((CodeableConcept) property.getValues().get(0)).getCoding();
        for (Coding coding : codings) {
            bothSystemAndCodePresent |= (coding.getSystem() != null && coding.getCode() != null);
        }
        return bothSystemAndCodePresent;
    }

}
