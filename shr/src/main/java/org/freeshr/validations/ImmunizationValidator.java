package org.freeshr.validations;


import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.INVALID_DOSAGE_QUANTITY;

@Component
public class ImmunizationValidator implements Validator<AtomEntry<? extends Resource>> {

    private static final Logger logger = LoggerFactory.getLogger(ImmunizationValidator.class);
    public static final String DOSE_QUANTITY = "doseQuantity";
    private DoseQuantityValidator doseQuantityValidator;
    private UrlValidator urlValidator;


    @Autowired
    public ImmunizationValidator(DoseQuantityValidator doseQuantityValidator, UrlValidator urlValidator) {
        this.doseQuantityValidator = doseQuantityValidator;
        this.urlValidator = urlValidator;
    }

    @Override
    public List<ValidationMessage> validate(ValidationSubject<AtomEntry<? extends Resource>> subject) {
        AtomEntry<? extends Resource> atomEntry = subject.extract();
        return validateDosageQuantity(atomEntry);
    }

    private List<ValidationMessage> validateDosageQuantity(AtomEntry<? extends Resource> atomEntry) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        Property property = atomEntry.getResource().getChildByName(DOSE_QUANTITY);
        if (!property.getName().equals(DOSE_QUANTITY) || !property.hasValues()) return validationMessages;

        Quantity doseQuantity = (Quantity) property.getValues().get(0);
        if (doseQuantityValidator.isReferenceUrlNotFound(doseQuantity)
                || !urlValidator.isValid(doseQuantity
                .getSystemSimple())) return validationMessages;

        ConceptLocator.ValidationResult validationResult = doseQuantityValidator.validate(doseQuantity);
        if (null == validationResult) return validationMessages;

        logger.debug("Medication-Prescription DosageQuantity Code is invalid.");

        validationMessages.add(new ValidationMessage(null,
                ResourceValidator.INVALID, atomEntry.getId(),
                INVALID_DOSAGE_QUANTITY,
                OperationOutcome.IssueSeverity.error));
        return validationMessages;
    }


}
