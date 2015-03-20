package org.freeshr.utils.log4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class AccessLogFilter extends Filter {
    @Override
    public int decide(LoggingEvent event) {
        return StringUtils.startsWith(event.getMessage().toString(), "ACCESS:") ? 1 : -1;
    }
}
