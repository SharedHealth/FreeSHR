package org.freeshr.validations.bundle;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.utils.FhirResourceHelper;
import org.freeshr.validations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.utils.UrlUtil.extractFacilityId;

@Component
public class FacilityValidator implements ShrValidator<Bundle> {

    public static final String INVALID_SERVICE_PROVIDER = "Invalid Service Provider";
    private final static Logger logger = LoggerFactory.getLogger(FacilityValidator.class);
    private final HIEFacilityValidator hieFacilityValidator;

    @Autowired
    public FacilityValidator(HIEFacilityValidator hieFacilityValidator) {
        this.hieFacilityValidator = hieFacilityValidator;
    }

    @Override
    public List<ShrValidationMessage> validate(ValidationSubject<Bundle> subject) {
        Bundle bundle = subject.extract();
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        Encounter encounter = identifyEncounter(bundle);
        String serviceProviderUrl = getServiceProviderRefUrl(encounter);
        if (StringUtils.isBlank(serviceProviderUrl)) {
            logger.debug("Encounter.serviceProvider (facility) is not specified");
            validationMessages.add(new ShrValidationMessage(Severity.INFORMATION, "Encounter", "notfound", INVALID_SERVICE_PROVIDER));
            //We can't throw error as the serviceProvider may be just a provider
            return validationMessages;
        }
        if (!hieFacilityValidator.validate(serviceProviderUrl)) {
            logger.debug("Encounter failed for invalid facility URL");
            validationMessages.add(new ShrValidationMessage(Severity.ERROR, "Encounter", "invalid", INVALID_SERVICE_PROVIDER + ":" + serviceProviderUrl));
            return validationMessages;
        }

        logger.debug(String.format("Encounter validated for valid facility %s", extractFacilityId(serviceProviderUrl)));
        return validationMessages;
    }

    private Encounter identifyEncounter(Bundle bundle) {
        List<Composition> compositions = FhirResourceHelper.findBundleResourcesOfType(bundle, Composition.class);
        return (Encounter) FhirResourceHelper.findBundleResourceByRef(bundle, compositions.get(0).getEncounter());
    }

    private String getServiceProviderRefUrl(Encounter encounter) {
        return (encounter != null) ? encounter.getServiceProvider().getReference().getValue() : null;
    }



}