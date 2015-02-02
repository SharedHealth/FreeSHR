package org.freeshr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static final String UTC_DATE_IN_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";
    public static final String UTC_DATE_IN_SECS_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
    public static final String SIMPLE_DATE_WITH_SECS_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_IN_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String ISO_DATE_IN_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String ISO_DATE_IN_HOUR_MIN_FORMAT="yyyy-MM-dd'T'HH:mmZ";

    public static final String[] DATE_FORMATS = new String[]{
            ISO_DATE_IN_MILLIS_FORMAT, ISO_DATE_IN_SECS_FORMAT,
            ISO_DATE_IN_HOUR_MIN_FORMAT,
            UTC_DATE_IN_MILLIS_FORMAT, UTC_DATE_IN_SECS_FORMAT,
            SIMPLE_DATE_WITH_SECS_FORMAT, SIMPLE_DATE_FORMAT};

    public static String getCurrentTimeInUTCString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(UTC_DATE_IN_MILLIS_FORMAT);
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

    public static Date parseDate(String date, String... formats) throws ParseException {
        return org.apache.commons.lang3.time.DateUtils.parseDate(date, formats);
    }

    public static Date parseDate(String date) {
        try {
            return parseDate(date, DateUtil.DATE_FORMATS);
        } catch (ParseException e) {
            throw new RuntimeException("invalid date:" + date);
        }
    }

    public static String toUTCString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(UTC_DATE_IN_MILLIS_FORMAT);
        //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static String toISOString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATE_IN_MILLIS_FORMAT);
        //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static String getCurrentTimeInISOString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_DATE_IN_MILLIS_FORMAT);
        return dateFormat.format(new Date());
    }

    public static String toDateString(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }
}
