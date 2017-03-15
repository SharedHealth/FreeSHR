package org.freeshr.validations.resource;

import org.freeshr.utils.FhirFeedUtil;
import org.hl7.fhir.dstu3.model.Quantity;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoseQuantityValidatorTest {

    @Test
    public void shouldCheckForReferenceUrlPresence() throws Exception {

        DoseQuantityValidator doseQuantityValidator = new DoseQuantityValidator(null, new FhirFeedUtil());
        assertTrue(doseQuantityValidator.isReferenceUrlNotFound((Quantity) null));
        assertTrue(doseQuantityValidator.isReferenceUrlNotFound(new Quantity()));

        Quantity quantityWithReference = new Quantity();
        quantityWithReference.setSystem("some url");
        assertFalse(doseQuantityValidator.isReferenceUrlNotFound(quantityWithReference));
    }

}