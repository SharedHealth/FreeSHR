package org.freeshr.validations;


import org.hl7.fhir.instance.model.DateAndTime;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;


@Component
public class DateValidator {
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    public boolean isValidPeriod(DateAndTime startDate, DateAndTime endDate) {
        return !(startDate != null && endDate != null)
                || isValidDate(startDate) && isValidDate(endDate) &&
                (startDate.before(endDate) || (startDate.toString()).equals(endDate.toString()));
    }

    public boolean isValidDate(DateAndTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
        try {
            formatter.parseDateTime(String.format("%s-%s-%s", dateTime.getDay(), dateTime.getMonth(), dateTime.getYear()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
