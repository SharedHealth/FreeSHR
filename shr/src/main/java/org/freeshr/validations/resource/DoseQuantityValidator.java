package org.freeshr.validations.resource;


import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.TRConceptValidator;
import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.dstu3.hapi.validation.IValidationSupport;
import org.hl7.fhir.dstu3.model.Quantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class DoseQuantityValidator {
    public static final String DOSE_QUANTITY = "doseQuantity";
    private TRConceptValidator conceptValidator;
    private FhirFeedUtil fhirFeedUtil;

    @Autowired
    public DoseQuantityValidator(TRConceptValidator conceptValidator, FhirFeedUtil fhirFeedUtil) {
        this.conceptValidator = conceptValidator;
        this.fhirFeedUtil = fhirFeedUtil;
    }

    public boolean isReferenceUrlNotFound(Quantity doseQuantity) {
        return doseQuantity == null || isEmpty(doseQuantity.getSystem());
    }

    public boolean hasReferenceUrlAndCode(Quantity doseQuantity) {
        return doseQuantity != null && isNotEmpty(doseQuantity.getSystem()) && isNotEmpty(doseQuantity.getCode());
    }

    public IValidationSupport.CodeValidationResult validate(Quantity doseQuantity) {
        if (StringUtils.isNotBlank(doseQuantity.getSystem())) {
            if (conceptValidator.isCodeSystemSupported(fhirFeedUtil.getFhirContext(), doseQuantity.getSystem())) {
                return conceptValidator.validateCode(fhirFeedUtil.getFhirContext(), doseQuantity.getSystem(), doseQuantity.getCode(), doseQuantity.getUnit());
            }
        }
        return null;
    }
}
