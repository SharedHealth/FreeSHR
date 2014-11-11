package org.freeshr.data;


import org.freeshr.application.fhir.EncounterBundle;

import static org.freeshr.utils.FileUtil.asString;

public class EncounterBundleData {

    public static final String HEALTH_ID = "5893922485019082753";

    public static EncounterBundle withValidEncounter() {
        return encounter(HEALTH_ID, asString("xmls/encounters/encounter.xml"));
    }
    public static EncounterBundle withNewValidEncounter() {
        return encounter(HEALTH_ID, asString("xmls/encounters/encounter_new.xml"));
    }

    public static EncounterBundle withInvalidConcept() {
        return encounter(HEALTH_ID, asString("xmls/encounters/invalid_concept.xml"));
    }

    public static EncounterBundle withInvalidReferenceTerm() {
        return encounter(HEALTH_ID, asString("xmls/encounters/invalid_ref.xml"));
    }

    public static EncounterBundle encounter(String healthId, String content) {
        EncounterBundle encounter = new EncounterBundle();
        encounter.setEncounterContent(content);
        encounter.setHealthId(healthId);
        return encounter;
    }
}
