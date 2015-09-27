package org.freeshr.validations.resource;


import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import org.freeshr.validations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.freeshr.validations.ValidationMessages.INVALID_DOSAGE_QUANTITY;

@Component
public class ImmunizationValidator implements SubResourceValidator {

    public static final String IMMUNIZATION_DOSE_QUANTITY_LOCATION = "f:Immunization/f:doseQuantity";
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
        return resource instanceof ca.uhn.fhir.model.dstu2.resource.Immunization;
    }

    @Override
    public List<ShrValidationMessage> validate(Object resource) {
        Immunization immunization = (Immunization) resource;
        return validateDosageQuantity(immunization);
    }

    private List<ShrValidationMessage> validateDosageQuantity(Immunization immunization) {
        List<ShrValidationMessage> validationMessages = new ArrayList<>();
        SimpleQuantityDt doseQuantity = immunization.getDoseQuantity();
        if (doseQuantity.isEmpty()) {
            return validationMessages;
        }

        if (doseQuantityValidator.isReferenceUrlNotFound(doseQuantity) || !urlValidator.isValid(doseQuantity.getSystem()))
            return validationMessages;

        if (doseQuantityValidator.validate(doseQuantity) == null) {
            return validationMessages;
        }

        logger.debug("Immunization DosageQuantity is invalid." + immunization.getId().getValue());

        validationMessages.add(new ShrValidationMessage(Severity.ERROR, IMMUNIZATION_DOSE_QUANTITY_LOCATION, "invalid",
                INVALID_DOSAGE_QUANTITY + ":Immunization:" + immunization.getId().getValue()));
        return validationMessages;
    }
}
