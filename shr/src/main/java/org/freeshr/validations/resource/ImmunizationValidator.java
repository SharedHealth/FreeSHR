package org.freeshr.validations.resource;


import org.freeshr.validations.Severity;
import org.freeshr.validations.ShrValidationMessage;
import org.freeshr.validations.SubResourceValidator;
import org.freeshr.validations.UrlValidator;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.INVALID_DOSAGE_QUANTITY;

@Component
public class ImmunizationValidator implements SubResourceValidator {

    public static final String IMMUNIZATION_DOSE_QUANTITY_LOCATION = "Bundle.entry[%s].resource.doseQuantity";
    private static final Logger logger = LoggerFactory.getLogger(ImmunizationValidator.class);
    private DoseQuantityValidator doseQuantityValidator;
    private UrlValidator urlValidator;

    @Autowired
    public ImmunizationValidator(DoseQuantityValidator doseQuantityValidator, UrlValidator urlValidator) {
        this.doseQuantityValidator = doseQuantityValidator;
        this.urlValidator = urlValidator;
    }


    @Override
    public boolean validates(Object resource) {
        return resource instanceof Immunization;
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource, int entryIndex) {
        Immunization immunization = (Immunization) resource;
        return validateDosageQuantity(immunization, entryIndex);
    }

    private List<ShrValidationMessage> validateDosageQuantity(Immunization immunization, int entryIndex) {
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        SimpleQuantity doseQuantity = immunization.getDoseQuantity();
        if (doseQuantity.isEmpty()) {
            return validationMessages;
        }

        if (!doseQuantityValidator.hasReferenceUrlAndCode(doseQuantity) || !urlValidator.isValid(doseQuantity.getSystem()))
            return validationMessages;

        if (doseQuantityValidator.validate(doseQuantity) == null) {
            return validationMessages;
        }

        logger.error("Immunization DosageQuantity is invalid." + immunization.getId());

        String location = String.format(IMMUNIZATION_DOSE_QUANTITY_LOCATION, entryIndex);
        validationMessages.add(new ShrValidationMessage(Severity.ERROR, location, "invalid",
                INVALID_DOSAGE_QUANTITY + ":Immunization:" + immunization.getId()));
        return validationMessages;
    }
}
