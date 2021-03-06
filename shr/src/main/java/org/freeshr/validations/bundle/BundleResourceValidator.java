package org.freeshr.validations.bundle;

import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.ShrValidator;
import org.freeshr.validations.SubResourceValidator;
import org.freeshr.validations.ValidationSubject;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BundleResourceValidator implements ShrValidator<Bundle> {
    private List<SubResourceValidator> subResourceValidators;

    @Autowired
    public BundleResourceValidator(List<SubResourceValidator> subResourceValidators) {
        this.subResourceValidators = subResourceValidators;
    }

    public List<ShrValidationMessage> validate(ValidationSubject<Bundle> subject) {
        Bundle bundle = subject.extract();
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
        for (int index = 0; index < entries.size(); index++) {
            Bundle.BundleEntryComponent entry = entries.get(index);
            Resource resource = entry.getResource();
            List<SubResourceValidator> validators = findSubResourceValidator(resource);
            if (!validators.isEmpty()) {
                for (SubResourceValidator validator : validators) {
                    validationMessages.addAll(validator.validate(resource, index + 1));
                }
            }
        }
        return validationMessages;
    }

    private List<SubResourceValidator> findSubResourceValidator(Resource resource) {
        List<SubResourceValidator> validators = new ArrayList<>();
        for (SubResourceValidator subResourceValidator : subResourceValidators) {
            if (subResourceValidator.validates(resource)) {
                validators.add(subResourceValidator);
            }
        }
        return validators; //DefaultValidator?
    }
}
