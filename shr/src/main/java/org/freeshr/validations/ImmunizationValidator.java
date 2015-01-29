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

import static org.freeshr.domain.ErrorMessageBuilder.INVALID_DOSAGE_QUANTITY;
import static org.freeshr.domain.ErrorMessageBuilder.buildValidationMessage;

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
        ArrayList<ValidationMessage> validationMessages = new ArrayList<>();

        validateDosageQuantity(atomEntry, validationMessages);

        return validationMessages;
    }

    private void validateDosageQuantity(AtomEntry<? extends Resource> atomEntry, ArrayList<ValidationMessage> validationMessages) {
        String id = atomEntry.getId();

        Property property = atomEntry.getResource().getChildByName(DOSE_QUANTITY);

        if (!property.getName().equals(DOSE_QUANTITY) || !property.hasValues())
            return;

        Quantity doseQuantity = (Quantity) property.getValues().get(0);

        if (!doseQuantityValidator.isReferenceUrlNotFound(doseQuantity) && urlValidator.isValid(doseQuantity.getSystemSimple())) {
            ConceptLocator.ValidationResult validationResult = doseQuantityValidator.validate(doseQuantity);
            if (validationResult != null) {
                logger.error("Medication-Prescription DosageQuantity Code is invalid:");
                validationMessages.add(buildValidationMessage(id, ResourceValidator.INVALID, INVALID_DOSAGE_QUANTITY, OperationOutcome.IssueSeverity.error));
            }
        }
    }


    }
