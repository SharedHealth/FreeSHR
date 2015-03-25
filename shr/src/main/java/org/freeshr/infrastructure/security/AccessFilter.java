package org.freeshr.infrastructure.security;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.freeshr.utils.CollectionUtils;
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
        if (userInfo.hasCatchment(catchment)) {
            return userInfo.isNotDatasenseFacility();
        }
        throw new Forbidden(String.format("Access to catchment [%s] is denied for user [%s]", catchment, userInfo.getId()));
    }

    public static List<EncounterBundle> filterEncounters(boolean isRestrictedAccess, List<EncounterBundle> encounterBundles) {
        if (!isRestrictedAccess) return encounterBundles;
        List<EncounterBundle> filteredEncounterBundle = new ArrayList<>();
        for (EncounterBundle encounterBundle : encounterBundles) {
            if (isConfidentialEncounter(encounterBundle)) {
                continue;
            }
            filteredEncounterBundle.add(encounterBundle);
        }
        return filteredEncounterBundle;
    }

    public static boolean isConfidentialEncounter(EncounterBundle encounterBundle) {
        return encounterBundle.getEncounterConfidentiality().ordinal() > Confidentiality.Normal.ordinal()
                || encounterBundle.getPatientConfidentiality().ordinal() > Confidentiality.Normal.ordinal();
    }

    public static boolean isConfidentialPatient(List<EncounterBundle> encounterBundles) {
        if (CollectionUtils.isNotEmpty(encounterBundles)) {
            return encounterBundles.get(0).getPatientConfidentiality().ordinal() > Confidentiality.Normal.ordinal();
        }
        return false;
    }
}
