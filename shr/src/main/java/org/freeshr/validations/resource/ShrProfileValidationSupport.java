package org.freeshr.validations.resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ShrProfileValidationSupport extends DefaultProfileValidationSupport {
    @Override
    @Cacheable(value = "shrProfileCache", unless = "#result == null")
    public <T extends IBaseResource> T fetchResource(FhirContext theContext, Class<T> theClass, String theUri) {
        return super.fetchResource(theContext, theClass, theUri);
    }
}
