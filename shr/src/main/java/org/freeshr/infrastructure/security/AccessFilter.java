package org.freeshr.infrastructure.security;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.CollectionUtils;

import java.util.List;

import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.UserProfile.FACILITY_TYPE;
import static org.freeshr.infrastructure.security.UserProfile.PROVIDER_TYPE;

public class AccessFilter {
    public Boolean isAccessRestrictedToEncounterFetchForPatient(String healthId, UserInfo userInfo) {
        if (userInfo.getProperties().getPatientHid() != null && userInfo.getProperties().getPatientHid().equals(healthId)) {
            return false;
        } else if (userInfo.getProperties().isShrSystemAdmin()) {
            return false;
        } else if (userInfo.getProperties().getFacilityId() != null || userInfo.getProperties().getProviderId() != null) {
            return true;
        }
        return null;
    }

    public Boolean isAccessRestrictedToEncounterFetchForCatchment(String catchment, UserInfo userInfo) {
        if (userInfo.getProperties().hasCatchmentForProfileType(catchment, asList(FACILITY_TYPE, PROVIDER_TYPE))) {
            return !userInfo.getProperties().isShrSystemAdmin();
        }
        return null;
    }

    public boolean isConfidentialPatient(List<EncounterBundle> encounterBundles) {
        if (CollectionUtils.isNotEmpty(encounterBundles)) {
            return encounterBundles.get(0).isConfidentialPatient();
        }
        return false;
    }
}
