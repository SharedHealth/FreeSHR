package org.freeshr.validations.resource;


import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.TRConceptValidator;
import org.hl7.fhir.instance.model.Quantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class DoseQuantityValidator {
    public static final String DOSE_QUANTITY = "doseQuantity";
    private TRConceptValidator conceptValidator;

    @Autowired
    public DoseQuantityValidator(TRConceptValidator conceptValidator) {
        this.conceptValidator = conceptValidator;
    }

    public boolean isReferenceUrlNotFound(Quantity doseQuantity) {
        return doseQuantity == null || isEmpty(doseQuantity.getSystem());
    }

    public Object validate(Quantity doseQuantity) {
        return conceptValidator.validateCode(doseQuantity.getSystem(), doseQuantity.getCode(), DOSE_QUANTITY);
    }

    public boolean isReferenceUrlNotFound(QuantityDt doseQuantity) {
        return doseQuantity == null || isEmpty(doseQuantity.getSystem());
    }

    public ca.uhn.fhir.validation.IValidationSupport.CodeValidationResult validate(QuantityDt doseQuantity) {
        if (StringUtils.isNotBlank(doseQuantity.getSystem())) {
            if (conceptValidator.isCodeSystemSupported(doseQuantity.getSystem())) {
                return conceptValidator.validateCode(doseQuantity.getSystem(), doseQuantity.getCode(), doseQuantity.getUnit());
            }
        }
        return null;
    }
}
