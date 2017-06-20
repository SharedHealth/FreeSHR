package org.freeshr.utils;

import ca.uhn.fhir.model.api.IResource;
import org.apache.commons.lang3.*;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.ArrayList;
import java.util.List;

public class FhirResourceHelper {

    public static <T> List<T> findBundleResourcesOfType(Bundle bundle, Class<T> type) {
        ArrayList<T> resourceList = new ArrayList<T>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();
            if (resource.getClass().isAssignableFrom(type)) {
                resourceList.add((T) resource);
            }
        }
        return resourceList;
    }

    public static Resource findBundleResourceByRef(Bundle bundle, Reference resourceRef) {
        IIdType resourceReference = resourceRef.getReferenceElement();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource entryResource = entry.getResource();
            IdType entryResourceId = entryResource.getIdElement();
            boolean hasFullUrlDefined = !org.apache.commons.lang3.StringUtils.isBlank(entry.getFullUrl());

            if (resourceReference.hasResourceType() && entryResourceId.hasResourceType()
                    && entryResourceId.getValue().equals(resourceReference.getValue()) ) {
                return entryResource;
            } else if (entryResourceId.getIdPart().equals(resourceReference.getIdPart())) {
                return entryResource;
            } else if (hasFullUrlDefined) {
                if (entry.getFullUrl().endsWith(resourceReference.getIdPart())) {
                    return entryResource;
                }
            }
        }
        //logger.warn("Could not determine resource for reference:" + resourceReference);
        return null;
    }

    public static List<Bundle.BundleEntryComponent> getBundleEntriesForResource(Bundle bundle, String resourceName) {
        List<Bundle.BundleEntryComponent> resourceEntries = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();
            if (resource.getResourceType().name().equalsIgnoreCase(resourceName)) {
                resourceEntries.add(entry);
            }
        }
        return resourceEntries;
    }

    public static boolean hasTRCoding(List<Coding> codings) {
        for (Coding coding : codings) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(coding.getSystem()) && coding.getSystem().contains("/tr/concepts/")) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(coding.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }
}
