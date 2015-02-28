package org.freeshr.validations;


import org.freeshr.application.fhir.TRConceptLocator;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class DoseQuantityValidator {
    public static final String DOSE_QUANTITY = "doseQuantity";
    private TRConceptLocator trConceptLocator;
    @Autowired
    public DoseQuantityValidator(TRConceptLocator trConceptLocator) {
        this.trConceptLocator = trConceptLocator;
    }

    public boolean isReferenceUrlNotFound(Quantity doseQuantity) {
        return doseQuantity == null || isEmpty(doseQuantity.getSystemSimple());
    }

    public ConceptLocator.ValidationResult validate(Quantity doseQuantity) {
        return trConceptLocator.validate(doseQuantity.getSystemSimple(),
                doseQuantity.getCodeSimple(), DOSE_QUANTITY);
    }
}
