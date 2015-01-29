package org.freeshr.validations;


import org.hl7.fhir.instance.model.DateAndTime;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;


@Component
public class DateValidator {

    private static final String DATE_FORMAT = "dd-MM-yyyy";

    //NOTE:Assuming:When Both Dates are null or one is null,there is no Period.So Valid one
    public boolean isValidPeriod(DateAndTime startDate, DateAndTime endDate) {
        if (startDate != null && endDate != null) {
            if (isValidDate(startDate) && isValidDate(endDate) && (startDate.before(endDate) || (startDate.toString()).equals(endDate.toString()))) {
                return true;
            }
            return false;
        }

        return true;
    }


    public boolean isValidDate(DateAndTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return isValidDate(getDateInStringFormat(dateTime));


    }

    public boolean isValidDate(String dateToValidate) {
        if (dateToValidate == null) {
            return false;
        }
        try {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            simpleDateFormat.setLenient(false);
            simpleDateFormat.parse(dateToValidate);

        } catch (ParseException ex) {

            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private String getDateInStringFormat(DateAndTime dateAndTime) {

        return dateAndTime.getDay() + "-" + dateAndTime.getMonth() + "-" + dateAndTime.getYear();
    }
}
