package org.freeshr.infrastructure.security;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.Confidentiality;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserProfile.FACILITY_TYPE;
import static org.freeshr.infrastructure.security.UserProfile.PROVIDER_TYPE;

public class AccessFilter {
    public static boolean isAccessRestrictedToEncounterFetchForPatient(String healthId, UserInfo userInfo) {
        if (userInfo.getProperties().getPatientHid() != null && userInfo.getProperties().getPatientHid().equals(healthId)) {
            return false;
        } else if (userInfo.getProperties().isShrSystemAdmin()) {
            return false;
        } else if (userInfo.getProperties().getFacilityId() != null || userInfo.getProperties().getProviderId() != null) {
            return true;
        }
        throw new Forbidden(String.format("Access is denied to user %s for patient %s", userInfo.getProperties().getId(), healthId));
    }

    public static boolean isAccessRestrictedToEncounterFetchForCatchment(String catchment, UserInfo userInfo) {
        if (userInfo.getProperties().hasCatchmentForProfileType(catchment, asList(FACILITY_TYPE, PROVIDER_TYPE))) {
            return !userInfo.getProperties().isShrSystemAdmin();
        }
        throw new Forbidden(String.format("Access is denied to user %s for catchment %s", userInfo.getProperties().getId(), catchment));
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
