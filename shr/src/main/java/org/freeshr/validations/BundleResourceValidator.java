package org.freeshr.validations;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BundleResourceValidator implements ShrValidator<Bundle> {
    private List<SubResourceValidator> subResourceValidators;

    @Autowired
    public List<ShrValidationMessage> validate(ValidationSubject<Bundle> subject) {
        Bundle bundle = subject.extract();
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        for (Bundle.Entry entry : bundle.getEntry()) {
            IResource resource = entry.getResource();
            SubResourceValidator validator = findSubResourceValidator(resource);
            if (validator != null) {
                validationMessages.addAll(validator.validate(resource));
            }
        }
        return validationMessages;
    }

    private SubResourceValidator findSubResourceValidator(IResource resource) {
        for (SubResourceValidator subResourceValidator : subResourceValidators) {
            if (subResourceValidator.validates(resource)) {
                return subResourceValidator;
            }
        }
        return null; //DefaultValidator?
    }
}
