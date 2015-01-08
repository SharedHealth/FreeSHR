package org.freeshr.validations;


import org.freeshr.application.fhir.TRConceptLocator;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DoseQuantityValidator {

    public static final String DOSE_QUANTITY = "doseQuantity";

    @Autowired
    private TRConceptLocator trConceptLocator;

    public boolean isReferenceUrlNotFound(Quantity doseQuantity) {
        String url = null;
        if (doseQuantity == null || (url = doseQuantity.getSystemSimple()) == null || url.isEmpty()) {
            return true;
        }
        return false;
    }

    public ConceptLocator.ValidationResult validate(Quantity doseQuantity) {
        String url = doseQuantity.getSystemSimple();
        return trConceptLocator.validate(url, doseQuantity.getCodeSimple(), DOSE_QUANTITY);
    }
}
