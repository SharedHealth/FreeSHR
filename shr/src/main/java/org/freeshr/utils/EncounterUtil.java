package org.freeshr.utils;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.config.SHRProperties;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.infrastructure.security.UserProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncounterUtil {
    public static List<EncounterBundle> filter(List<EncounterBundle> encounterBundles, UserInfo userInfo, SHRProperties shrProperties) {

        List<UserProfile> userProfiles = userInfo.getUserProfiles();
        for (UserProfile userProfile : userProfiles) {
            if (userProfile.isPatientType()) {
                return encounterBundles;
            }
        }

        for (UserProfile userProfile : userProfiles) {
            if (userProfile.isFaciltiyType()) {
                String[] datasenseFacilityCodes = shrProperties.getDatasenseFacilityCodes();
                if(Arrays.asList(datasenseFacilityCodes).contains(userProfile.getId()))
                    return encounterBundles;
            }
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

    private static boolean toBeRestricted(EncounterBundle encounterBundle) {
        return encounterBundle.getEncounterConfidentiality().equals(Confidentiality.VeryRestricted)
                || encounterBundle.getPatientConfidentiality().equals(Confidentiality.VeryRestricted)
                || encounterBundle.getEncounterConfidentiality().equals(Confidentiality.Restricted)
                || encounterBundle.getPatientConfidentiality().equals(Confidentiality.Restricted);
    }
}
