package org.freeshr.data;


import org.freeshr.application.fhir.EncounterBundle;

import static org.freeshr.utils.FileUtil.asString;

public class EncounterBundleData {

    public static final String HEALTH_ID = "98001046534";

    public static EncounterBundle withValidEncounter() {
        return withValidEncounter(HEALTH_ID);
    }

    public static EncounterBundle withValidEncounter(String healthId) {
        return encounter(healthId, asString("xmls/encounters/dstu2/p98001046534_encounter_with_diagnoses_with_local_refs.xml"));
    }

    public static EncounterBundle withContentForHealthId(String healthId, String filename) {
        return encounter(healthId, asString(filename));
    }

    public static EncounterBundle encounter(String healthId, String content) {
        EncounterBundle encounter = new EncounterBundle();
        encounter.setEncounterContent(content);
        encounter.setHealthId(healthId);
        return encounter;
    }

    public static EncounterBundle withNewEncounterForPatient(String healthId) {
        return encounter(healthId, asString(String.format("xmls/encounters/dstu2/p%s_encounter_with_diagnoses_with_local_refs.xml", healthId)));
    }
}
