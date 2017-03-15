package org.freeshr.validations.bundle;

import org.freeshr.config.SHRProperties;
import org.freeshr.validations.*;
import org.freeshr.validations.providerIdentifiers.ClinicalResourceProviderIdentifier;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class ProviderValidator implements ShrValidator<Bundle> {

    private static final Logger logger = LoggerFactory.getLogger(ProviderValidator.class);
    private List<ClinicalResourceProviderIdentifier> clinicalResourceProviderIdentifiers;
    private SHRProperties shrProperties;

    @Autowired
    public ProviderValidator(List<ClinicalResourceProviderIdentifier> clinicalResourceProviderIdentifiers,
                             SHRProperties shrProperties) {
        this.clinicalResourceProviderIdentifiers = clinicalResourceProviderIdentifiers;
        this.shrProperties = shrProperties;
    }

    @Override
    public List<ShrValidationMessage> validate(ValidationSubject<Bundle> subject) {
        Bundle bundle = subject.extract();
        List<Bundle.BundleEntryComponent> entryList = bundle.getEntry();
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : entryList) {
            Resource resource = entry.getResource();
            for (ClinicalResourceProviderIdentifier clinicalResourceProviderIdentifier : clinicalResourceProviderIdentifiers) {
                try {
                    if (!clinicalResourceProviderIdentifier.isValid(resource, shrProperties)) {
                        logger.error(String.format("Provider:Encounter failed for %s", ValidationMessages.INVALID_PROVIDER_URL));
                        validationMessages.add(
                                new ShrValidationMessage(Severity.ERROR, resource.getResourceType().name(), "invalid",
                                        String.format("%s in %s:%s",
                                                ValidationMessages.INVALID_PROVIDER_URL,
                                                resource.getResourceType().name(),
                                                resource.getId())));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Unable to reach provider registry ");
                }
            }
        }
        return validationMessages;
    }
}

