package org.freeshr.validations;

import org.freeshr.config.SHRProperties;
import org.freeshr.domain.ErrorMessageBuilder;
import org.freeshr.validations.ProviderSubResourceValidators.SubResourceProvider;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.domain.ErrorMessageBuilder.buildValidationMessage;
import static org.freeshr.validations.ResourceValidator.INVALID;
import static org.hl7.fhir.instance.model.OperationOutcome.IssueSeverity.error;

@Component
public class ProviderValidator implements Validator<AtomFeed> {


    private List<SubResourceProvider> subResourceProviders;
    private SHRProperties shrProperties;

    @Autowired
    public ProviderValidator(List<SubResourceProvider> subResourceProviders,
                             SHRProperties shrProperties) {
        this.subResourceProviders = subResourceProviders;
        this.shrProperties = shrProperties;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomFeed> subject) {
        AtomFeed atomFeed = subject.extract();
        List<AtomEntry<? extends Resource>> entryList = atomFeed.getEntryList();
        List<ValidationMessage> validationMessages = new ArrayList<>();
        for (AtomEntry<? extends Resource> entry : entryList) {
            Resource resource = entry.getResource();
            for (SubResourceProvider subResourceProvider : subResourceProviders) {
                if (!subResourceProvider.validateProvider(resource, shrProperties)) {
                    validationMessages.add(buildValidationMessage(entry.getId(), INVALID,
                            ErrorMessageBuilder.INVALID_PROVIDER_URL_PATTERN + " in " +
                                    resource.getResourceType().getPath(), error));
                    return validationMessages;
                }
            }
        }
        return validationMessages;
    }
}

