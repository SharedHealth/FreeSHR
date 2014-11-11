package org.freeshr.data;


import org.freeshr.application.fhir.EncounterBundle;

import static org.freeshr.utils.FileUtil.asString;

public class EncounterBundleData {

    public static final String HEALTH_ID = "5893922485019082753";

    public static EncounterBundle withValidEncounter() {
        return withValidEncounter(HEALTH_ID);
    }

    public static EncounterBundle withValidEncounter(String healthId) {
        return encounter(healthId, asString("xmls/encounters/encounter.xml"));
    }

    public static EncounterBundle withNewValidEncounter(String healthId) {
        return encounter(healthId, asString("xmls/encounters/encounter_new.xml"));
    }

    public static EncounterBundle withInvalidConcept() {
        return encounter(HEALTH_ID, asString("xmls/encounters/invalid_concept.xml"));
    }

    public static EncounterBundle encounterForUnknownPatient() {
        return encounter("1234", asString("xmls/encounters/encounter_health_id_1234.xml"));
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
