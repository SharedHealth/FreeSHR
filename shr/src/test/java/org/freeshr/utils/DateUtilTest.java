package org.freeshr.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

public class DateUtilTest {

    @Test
    public void shouldGiveCurrentDateInTheRightFormat() {
        DateTime date = new DateTime(new Date());
        String format = String.format("%d-%02d-%02d", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        assertThat(DateUtil.getCurrentTimeInUTCString().startsWith(format), is(true));
    }

    //@Test
    public void shouldParseUTCDatesFromString() throws Exception {
        parseDate("2015-02-17 11:32:24.638+0530", DateUtil.UTC_DATE_IN_MILLIS_FORMAT);
        parseDate("2015-02-17 11:35:17+0530", DateUtil.UTC_DATE_IN_SECS_FORMAT);
        parseDate("2015-02-17 11:35:36", DateUtil.SIMPLE_DATE_WITH_SECS_FORMAT);
        parseDate("2015-02-17", DateUtil.SIMPLE_DATE_FORMAT);
        parseDate("2015-02-17T11:36:11.587+0530", DateUtil.ISO_DATE_IN_MILLIS_FORMAT);
        parseDate("2015-02-17T11:37:16+0530", DateUtil.ISO_DATE_IN_SECS_FORMAT);
        parseDate("2015-02-17T11:37+0530", DateUtil.ISO_DATE_IN_HOUR_MIN_FORMAT);


        parseDate("2011-04-15T20:08:18.032Z", DateUtil.UTC_DATE_MILLIS_TZD_FORMAT);
        parseDate("2011-04-15T20:08:18Z", DateUtil.UTC_DATE_IN_SECS_TZD_FORMAT);
        parseDate("2011-04-15T20:08Z", DateUtil.UTC_DATE_IN_MIN_TZD_FORMAT);
        parseDate("2011-04-15T20Z", DateUtil.UTC_DATE_IN_HOUR_TZD_FORMAT);
        parseDate("2011-04-15TZ", DateUtil.UTC_DATE_IN_DATE_TZD_FORMAT);
        parseDate("2011-04-15Z", DateUtil.UTC_DATE_IN_SIMPLE_TZD_FORMAT);
    }

    @Test
    public void shouldParseDates() throws Exception {
        assertParsedDatesAreSame("2015-02-17 11:32:24.638+0530", DateUtil.UTC_DATE_IN_MILLIS_FORMAT);
        assertParsedDatesAreSame("2015-02-17 11:35:17+0530", DateUtil.UTC_DATE_IN_SECS_FORMAT);
        assertParsedDatesAreSame("2015-02-17 11:35:36", DateUtil.SIMPLE_DATE_WITH_SECS_FORMAT);
        assertParsedDatesAreSame("2015-02-17", DateUtil.SIMPLE_DATE_FORMAT);
        assertParsedDatesAreSame("2015-02-17T11:36:11.587+0530", DateUtil.ISO_DATE_IN_MILLIS_FORMAT);
        assertParsedDatesAreSame("2015-02-17T11:37:16+0530", DateUtil.ISO_DATE_IN_SECS_FORMAT);
        assertParsedDatesAreSame("2015-02-17T11:37+0530", DateUtil.ISO_DATE_IN_HOUR_MIN_FORMAT);


        assertParsedDatesAreSame("2011-04-15T20:08:18.032Z", DateUtil.UTC_DATE_MILLIS_TZD_FORMAT);
        assertParsedDatesAreSame("2011-04-15T20:08:18Z", DateUtil.UTC_DATE_IN_SECS_TZD_FORMAT);
        assertParsedDatesAreSame("2011-04-15T20:08Z", DateUtil.UTC_DATE_IN_MIN_TZD_FORMAT);
        assertParsedDatesAreSame("2011-04-15T20Z", DateUtil.UTC_DATE_IN_HOUR_TZD_FORMAT);
        assertParsedDatesAreSame("2011-04-15TZ", DateUtil.UTC_DATE_IN_DATE_TZD_FORMAT);
        assertParsedDatesAreSame("2011-04-15Z", DateUtil.UTC_DATE_IN_SIMPLE_TZD_FORMAT);
    }

    private void assertParsedDatesAreSame(String dateString, String format) throws ParseException {
        assertEquals(parseDate(dateString), parseDate(dateString, format));
    }

    private Date parseDate(String dateString) {
        Date date = DateUtil.parseDate(dateString);
        //System.out.println(date);
        return date;
    }

    private Date parseDate(String dateString, String format) throws ParseException {
        Date date = DateUtil.parseDate(dateString, format);
        //System.out.println(date);
        return date;
    }


}
