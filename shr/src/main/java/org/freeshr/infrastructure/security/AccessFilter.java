package org.freeshr.infrastructure.security;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.events.EncounterEvent;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.Confidentiality;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserProfile.FACILITY_TYPE;
import static org.freeshr.infrastructure.security.UserProfile.PROVIDER_TYPE;

public class AccessFilter {
    public static Boolean isAccessRestrictedToEncounterFetchForPatient(String healthId, UserInfo userInfo) {
        if (userInfo.getProperties().getPatientHid() != null && userInfo.getProperties().getPatientHid().equals(healthId)) {
            return false;
        } else if (userInfo.getProperties().isShrSystemAdmin()) {
            return false;
        } else if (userInfo.getProperties().getFacilityId() != null || userInfo.getProperties().getProviderId() != null) {
            return true;
        }
        return null;
    }

    public static Boolean isAccessRestrictedToEncounterFetchForCatchment(String catchment, UserInfo userInfo) {
        if (userInfo.getProperties().hasCatchmentForProfileType(catchment, asList(FACILITY_TYPE, PROVIDER_TYPE))) {
            return !userInfo.getProperties().isShrSystemAdmin();
        }
        return null;
    }

    public static List<EncounterEvent> filterEncounterEvents(boolean isRestrictedAccess, List<EncounterEvent> encounterEvents) {
        if (!isRestrictedAccess) return encounterEvents;
        List<EncounterEvent> filteredEncounterEvents = new ArrayList<>();
        for (EncounterEvent encounterEvent : encounterEvents) {
            if (encounterEvent.isConfidentialEncounter()) {
                continue;
            }
            filteredEncounterEvents.add(encounterEvent);
        }
        return filteredEncounterEvents;
    }

    public static boolean isConfidentialPatient(List<EncounterBundle> encounterBundles) {
        if (CollectionUtils.isNotEmpty(encounterBundles)) {
            return encounterBundles.get(0).getPatientConfidentiality().ordinal() > Confidentiality.Normal.ordinal();
        }
        return false;
    }
}
