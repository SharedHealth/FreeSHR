package org.freeshr.validations.resource;

import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.SubResourceValidator;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class EncounterResourceValidator implements SubResourceValidator {
    private static final String ENCOUNTER_TYPE_LOCATION_FORMAT = "Bundle.entry[%s].resource.type.coding";
    private static final String ENCOUNTER_TYPE_FROM_TR_MSG = "There must be an encounter type code from TR";

    @Override
    public boolean validates(Object resource) {
        return (resource instanceof Encounter);
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource, int entryIndex) {
        Encounter encounter = (Encounter) resource;
        CodeableConcept encounterType = encounter.getTypeFirstRep();
        if (encounterType.isEmpty() || encounterType.getCodingFirstRep().isEmpty()) {
            String location = String.format(ENCOUNTER_TYPE_LOCATION_FORMAT, entryIndex);
            return Arrays.asList(new ShrValidationMessage(Severity.ERROR, location,
                    "invalid", ENCOUNTER_TYPE_FROM_TR_MSG));
        }
        return Collections.emptyList();
    }
}
