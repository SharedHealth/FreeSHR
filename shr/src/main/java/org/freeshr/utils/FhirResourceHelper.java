package org.freeshr.utils;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.primitive.IdDt;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.ArrayList;
import java.util.List;

public class FhirResourceHelper {

    public static <T> List<T> findBundleResourcesOfType(Bundle bundle, Class<T> type) {
        ArrayList<T> resourceList = new ArrayList<T>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            IResource resource = (IResource) entry.getResource();
            if (resource.getClass().isAssignableFrom(type)) {
                resourceList.add((T) resource);
            }
        }
        return resourceList;
    }

    public static IResource findBundleResourceByRef(Bundle bundle, Reference resourceRef) {
        IIdType resourceReference = resourceRef.getReferenceElement();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            IResource entryResource = (IResource) entry.getResource();
            IdDt entryResourceId = entryResource.getId();
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
            IResource resource = (IResource) entry.getResource();
            if (resource.getResourceName().equalsIgnoreCase(resourceName)) {
                resourceEntries.add(entry);
            }
        }
        return resourceEntries;
    }
}
