package org.freeshr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static final String UTC_DATE_IN_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";
    public static final String UTC_DATE_IN_SECS_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
    public static final String[] DATE_FORMATS = new String[]{UTC_DATE_IN_MILLIS_FORMAT, UTC_DATE_IN_SECS_FORMAT, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};

    public static String fromUTCDate(Date aDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(UTC_DATE_IN_SECS_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(aDate);
    }

    public static String getCurrentTimeInUTC() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(UTC_DATE_IN_SECS_FORMAT);
        return dateFormat.format(new Date());
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getYearOf(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance.get(Calendar.YEAR);
    }

    public static Date parseDate(String date, String[] formats) throws ParseException {
        return org.apache.commons.lang3.time.DateUtils.parseDate(date, formats);
    }

    public static Date parseDate(String date) {
        try {
            return parseDate(date, DateUtil.DATE_FORMATS);
        } catch (ParseException e) {
            throw new RuntimeException("invalid date:" + date);
        }
    }
}
