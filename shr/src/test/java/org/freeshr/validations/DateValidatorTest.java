package org.freeshr.validations;

import org.hl7.fhir.instance.model.DateAndTime;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateValidatorTest {

    @Test
    public void shouldValidateDate() throws Exception {
        DateValidator dateValidator = new DateValidator();
        DateAndTime dateAndTime = new DateAndTime("2014-12-31T00:00:00+05:30");
        assertTrue(dateValidator.isValidDate(dateAndTime));
    }

    @Test
    public void shouldValidateInvalidDate() throws Exception {
        DateValidator dateValidator = new DateValidator();
        DateAndTime dateAndTime = new DateAndTime("2014-02-31T00:00:00+05:30");
        assertFalse(dateValidator.isValidDate(dateAndTime));
    }

    @Test
    public void shouldValidatePeriod() throws Exception {
        DateValidator dateValidator = new DateValidator();

        DateAndTime startDate = new DateAndTime("2014-12-31T00:00:00+05:30");
        DateAndTime endDate = new DateAndTime("2014-12-31T00:00:00+05:30");
        assertTrue(dateValidator.isValidPeriod(startDate, endDate));


        startDate = new DateAndTime("2014-12-31T00:00:00+05:30");
        endDate = null;
        assertTrue(dateValidator.isValidPeriod(startDate, endDate));


        startDate = null;
        endDate = new DateAndTime("2014-12-31T00:00:00+05:30");
        assertTrue(dateValidator.isValidPeriod(startDate, endDate));

        startDate = null;
        endDate = null;
        assertTrue(dateValidator.isValidPeriod(startDate, endDate));

    }

    @Test
    public void shouldValidateInvalidPeriod() throws Exception {
        DateValidator dateValidator = new DateValidator();

        DateAndTime startDate = new DateAndTime("2014-12-31T00:00:00+05:30");
        DateAndTime endDate = new DateAndTime("2014-11-30T00:00:00+05:30");
        assertFalse(dateValidator.isValidPeriod(startDate, endDate));

    }

}