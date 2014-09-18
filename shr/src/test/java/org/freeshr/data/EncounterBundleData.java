package org.freeshr.data;


import org.freeshr.application.fhir.EncounterBundle;

import static org.freeshr.utils.FileUtil.asString;

public class EncounterBundleData {

    public static EncounterBundle withValidEncounter(String healthId) {
        return encounter(healthId, asString("xmls/encounters/encounter.xml"));
    }
    public static EncounterBundle withNewValidEncounter(String healthId) {
        return encounter(healthId, asString("xmls/encounters/encounter_new.xml"));
    }

    public static EncounterBundle withInvalidConcept(String healthId) {
        return encounter(healthId, asString("xmls/encounters/invalid_concept.xml"));
    }

    public static EncounterBundle withInvalidReferenceTerm(String healthId) {
        return encounter(healthId, asString("xmls/encounters/invalid_ref.xml"));
    }

    public static EncounterBundle encounter(String healthId, String content) {
        EncounterBundle encounter = new EncounterBundle();
        encounter.setEncounterContent(content);
        encounter.setHealthId(healthId);
        return encounter;
    }
}
