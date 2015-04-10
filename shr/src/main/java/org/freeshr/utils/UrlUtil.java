package org.freeshr.utils;

import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

public class UrlUtil {
    public static String addLastUpdatedQueryParams(HttpServletRequest request, Date lastUpdateDate, String lastMarker)
            throws UnsupportedEncodingException {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(request.getRequestURL().toString());
        if (lastUpdateDate != null) {
            uriBuilder.queryParam("updatedSince", URLEncoder.encode(DateUtil.toISOString(lastUpdateDate), "UTF-8"));
        }
        if (!org.apache.commons.lang3.StringUtils.isBlank(lastMarker)) {
            uriBuilder.queryParam("lastMarker", lastMarker);
        }
        return uriBuilder.build().toString();
    }
}
