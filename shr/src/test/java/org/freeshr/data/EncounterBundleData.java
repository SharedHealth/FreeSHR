package org.freeshr.data;


import org.freeshr.application.fhir.EncounterBundle;

import static org.freeshr.utils.FileUtil.asString;

public class EncounterBundleData {

    public static EncounterBundle withValidEncounter(String healthId) {
        return encounter(healthId, asString("jsons/encounters/valid.json"));
    }

    public static EncounterBundle withInvalidConcept(String healthId) {
        return encounter(healthId, asString("jsons/encounters/invalid_concept.json"));
    }

    public static EncounterBundle withInvalidReferenceTerm(String healthId) {
        return encounter(healthId, asString("jsons/encounters/invalid_ref.json"));
    }

    public static EncounterBundle withDiagnosisHavingNoRefSystems(String healthId){
        return encounter(healthId, asString("jsons/encounters/diagnosis_system_missing.json"));
    }

    public static EncounterBundle withDiagnosisHavingAllValidRefSystems(String healthId){
        return encounter(healthId, asString("jsons/encounters/valid_multiple_ref.json"));
    }

    public static EncounterBundle withDiagnosisHavingFewValidRefSystems(String healthId){
        return encounter(healthId, asString("jsons/encounters/invalid_multiple_ref.json"));
    }

    private static EncounterBundle encounter(String healthId, String content) {
        EncounterBundle encounter = new EncounterBundle();
        encounter.setEncounterContent(content);
        encounter.setHealthId(healthId);
        return encounter;
    }


}
