package org.freeshr.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DateUtilTest {

    @Test
    public void shouldGiveCurrentDateInTheRightFormat() {
        DateUtil dateUtil = new DateUtil();
        DateTime date = new DateTime(new Date());
        String format = String.format("%d-%02d-%02d", date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
        assertThat(dateUtil.getCurrentTimeInUTCString().startsWith(format), is(true));
    }


}
