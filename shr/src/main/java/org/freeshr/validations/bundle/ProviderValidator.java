package org.freeshr.validations.bundle;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.freeshr.config.SHRProperties;
import org.freeshr.validations.*;
import org.freeshr.validations.providerIdentifiers.ClinicalResourceProviderIdentifier;
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
        List<Bundle.Entry> entryList = bundle.getEntry();
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        for (Bundle.Entry entry : entryList) {
            IResource resource = entry.getResource();
            for (ClinicalResourceProviderIdentifier clinicalResourceProviderIdentifier : clinicalResourceProviderIdentifiers) {
                if (!clinicalResourceProviderIdentifier.isValid(resource, shrProperties)) {
                    logger.debug(String.format("Provider:Encounter failed for %s", ValidationMessages.INVALID_PROVIDER_URL));
                    validationMessages.add(
                        new ShrValidationMessage(Severity.ERROR, resource.getResourceName(), "invalid",
                            String.format("%s in %s:%s",
                                    ValidationMessages.INVALID_PROVIDER_URL,
                                    resource.getResourceName(),
                                    resource.getId().getValue())));
                }
            }
        }
        return validationMessages;
    }
}

