package org.freeshr.utils;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.interfaces.encounter.ws.Forbidden;

import java.util.ArrayList;
import java.util.List;

public class AccessFilter {
    public static boolean isRestrictedAccessForEncounterFetchForPatient(String healthId, UserInfo userInfo) {
        if (userInfo.getPatientHid() != null && userInfo.getPatientHid().equals(healthId)) {
            return false;
        } else if (userInfo.getFacilityId() != null || userInfo.getProviderId() != null) {
            if (userInfo.isDatasenseFacility()) {
                return false;
            }
            return true;
        }
        throw new Forbidden(String.format("Access for patient [%s] is denied for user [%s]", healthId, userInfo.getId()));
    }

    public static boolean isRestrictedAccessForEncounterFetchForCatchment(String catchment, UserInfo userInfo) {
        if (!userInfo.hasCatchment(catchment)) {
            throw new Forbidden(String.format("Access for catchment [%s] is denied for user [%s]", catchment, userInfo.getId()));
        }
        if (userInfo.isDatasenseFacility()) {
            return false;
        }
        return true;
    }

    public static boolean checkAccessForEncounterPush(UserInfo userInfo) {
        if (userInfo.isDatasenseFacility()) {
            throw new Forbidden(String.format("Access for encounter post is denied for user [%s]", userInfo.getId()));
        }
        return true;
    }

    public static List<EncounterBundle> filterEncounters(boolean isRestrictedAccess, List<EncounterBundle> encounterBundles) {
        if (!isRestrictedAccess) {
            return encounterBundles;
        }
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
