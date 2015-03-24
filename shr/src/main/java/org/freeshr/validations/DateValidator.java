package org.freeshr.validations;


import org.hl7.fhir.instance.model.DateAndTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import java.util.Calendar;


@Component
public class DateValidator {
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    public boolean isValidPeriod(DateAndTime startDate, DateAndTime endDate) {
        boolean isAnyDateNull = !(startDate != null && endDate != null);
        return isAnyDateNull ||
                (isValidDate(startDate) && isValidDate(endDate) &&
                     (dateBefore(startDate, endDate) || datesAreEqual(startDate, endDate)));
    }

    private boolean dateBefore(DateAndTime startDate, DateAndTime endDate) {
        return startDate.before(endDate);
    }

    private boolean datesAreEqual(DateAndTime startDate, DateAndTime endDate) {
        Calendar start = startDate.toCalendar();
        Calendar end = endDate.toCalendar();
        return start.getTimeInMillis() == end.getTimeInMillis();
    }

    public boolean isValidDate(DateAndTime dateTime) {
        if (dateTime == null) return false;
        DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
        try {
            formatter.parseDateTime(String.format("%s-%s-%s", dateTime.getDay(), dateTime.getMonth(), dateTime.getYear()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
