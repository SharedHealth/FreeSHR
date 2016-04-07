package org.freeshr.utils;

import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

public class UrlUtil {
    public static String formUrlAndAddLastUpdatedQueryParams(HttpServletRequest request, Date lastUpdateDate, String lastMarker)
            throws UnsupportedEncodingException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(formRequestUrl(request));
        if (lastUpdateDate != null) {
            uriBuilder.queryParam("updatedSince", URLEncoder.encode(DateUtil.toISOString(lastUpdateDate), "UTF-8"));
        }
        if (!org.apache.commons.lang3.StringUtils.isBlank(lastMarker)) {
            uriBuilder.queryParam("lastMarker", URLEncoder.encode(lastMarker, "UTF-8"));
        }
        return uriBuilder.build().toString();
    }

    public static String formRequestUrl(HttpServletRequest request) {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(request.getRequestURL().toString());
        String scheme = request.getHeader("X-Forwarded-Proto");
        if(scheme != null) {
            uriBuilder.scheme(scheme);
        }
        return uriBuilder.build().toString();
    }

    public static String extractFacilityId(String facilityUrl){
        return facilityUrl.substring(facilityUrl.lastIndexOf('/') + 1, facilityUrl.lastIndexOf('.')).trim();

    }
}
