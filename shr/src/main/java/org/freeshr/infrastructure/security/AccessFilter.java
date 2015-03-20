package org.freeshr.infrastructure.security;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.freeshr.utils.Confidentiality;

import java.util.ArrayList;
import java.util.List;

public class AccessFilter {
    public static boolean isAccessRestrictedToEncounterFetchForPatient(String healthId, UserInfo userInfo) {
        if (userInfo.getPatientHid() != null && userInfo.getPatientHid().equals(healthId))
            return false;
        else if (userInfo.getFacilityId() != null || userInfo.getProviderId() != null) {
            return userInfo.isNotDatasenseFacility();
        }
        throw new Forbidden(String.format("Access for patient %s is denied for user %s", healthId, userInfo.getId()));
    }

    public static boolean isAccessRestrictedToEncounterFetchForCatchment(String catchment, UserInfo userInfo) {
        if (userInfo.hasCatchment(catchment)) {return userInfo.isNotDatasenseFacility();}
        throw new Forbidden(String.format("Access to catchment [%s] is denied for user [%s]", catchment, userInfo.getId()));
    }

    public static boolean validateAccessToSaveEncounter(UserInfo userInfo) {
        if (userInfo.isNotDatasenseFacility()) {return true;}
        throw new Forbidden(String.format("Access to save new encounter is denied for user [%s]", userInfo.getId()));
    }

    public static List<EncounterBundle> filterEncounters(boolean isRestrictedAccess, List<EncounterBundle> encounterBundles) {
        if (!isRestrictedAccess) return encounterBundles;
        List<EncounterBundle> filteredEncounterBundle = new ArrayList<>();
        for (EncounterBundle encounterBundle : encounterBundles) {
            if (toBeRestricted(encounterBundle)) {
                continue;
            }
            filteredEncounterBundle.add(encounterBundle);
        }
        return filteredEncounterBundle;
    }

    public static boolean toBeRestricted(EncounterBundle encounterBundle) {
        return encounterBundle.getEncounterConfidentiality().equals(Confidentiality.VeryRestricted)
                || encounterBundle.getPatientConfidentiality().equals(Confidentiality.VeryRestricted)
                || encounterBundle.getEncounterConfidentiality().equals(Confidentiality.Restricted)
                || encounterBundle.getPatientConfidentiality().equals(Confidentiality.Restricted);
    }
}
