package org.freeshr.utils;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.primitive.IdDt;
import org.apache.commons.lang3.*;

import java.util.ArrayList;
import java.util.List;

public class FhirResourceHelper {

    public static <T> List<T> findBundleResourcesOfType(Bundle bundle, Class<T> type) {
        ArrayList<T> resourceList = new ArrayList<T>();
        for (Bundle.Entry entry : bundle.getEntry()) {
            IResource resource = entry.getResource();
            if (resource.getClass().isAssignableFrom(type)) {
                resourceList.add((T) resource);
            }
        }
        return resourceList;
    }

    public static IResource findBundleResourceByRef(Bundle bundle, ResourceReferenceDt resourceRef) {
        IdDt resourceReference = resourceRef.getReference();
        for (Bundle.Entry entry : bundle.getEntry()) {
            IResource entryResource = entry.getResource();
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
}
