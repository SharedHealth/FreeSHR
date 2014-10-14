package org.freeshr.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static String fromUTCDate(Date aDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(aDate);
    }

    public static String getCurrentTimeInUTC() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        return dateFormat.format(new Date());
    }
}
