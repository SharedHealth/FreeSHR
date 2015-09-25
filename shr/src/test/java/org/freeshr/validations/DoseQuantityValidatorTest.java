package org.freeshr.validations;

import org.hl7.fhir.instance.model.Quantity;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoseQuantityValidatorTest {

    @Test
    public void shouldCheckForReferenceUrlPresence() throws Exception {

        DoseQuantityValidator doseQuantityValidator = new DoseQuantityValidator(null);
        assertTrue(doseQuantityValidator.isReferenceUrlNotFound(null));
        assertTrue(doseQuantityValidator.isReferenceUrlNotFound(new Quantity()));

        Quantity quantityWithReference = new Quantity();
        quantityWithReference.setSystem("some url");
        assertFalse(doseQuantityValidator.isReferenceUrlNotFound(quantityWithReference));
    }

}