package org.freeshr.validations;

import org.freeshr.config.SHRProperties;
import org.freeshr.validations.ProviderSubResourceValidators.ProviderSubresourceValidator;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ResourceValidator.INVALID;
import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity.error;

@Component
public class ProviderValidator implements Validator<AtomFeed> {


    private List<ProviderSubresourceValidator> providerSubresourceValidators;
    private SHRProperties shrProperties;

    @Autowired
    public ProviderValidator(List<ProviderSubresourceValidator> providerSubresourceValidators,
                             SHRProperties shrProperties) {
        this.providerSubresourceValidators = providerSubresourceValidators;
        this.shrProperties = shrProperties;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomFeed> subject) {
        AtomFeed atomFeed = subject.extract();
        List<AtomEntry<? extends Resource>> entryList = atomFeed.getEntryList();
        List<ValidationMessage> validationMessages = new ArrayList<>();
        for (AtomEntry<? extends Resource> entry : entryList) {
            Resource resource = entry.getResource();
            for (ProviderSubresourceValidator providerSubresourceValidator : providerSubresourceValidators) {
                if (providerSubresourceValidator.isValid(resource, shrProperties)) continue;
                validationMessages.add(new ValidationMessage(null, INVALID, entry.getId(), ValidationMessages
                        .INVALID_PROVIDER_URL_PATTERN + " in " +
                        resource.getResourceType().getPath(), error));
                return validationMessages;
            }
        }
        return validationMessages;
    }
}

