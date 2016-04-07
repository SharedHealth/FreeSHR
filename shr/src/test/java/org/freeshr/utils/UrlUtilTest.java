package org.freeshr.utils;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UrlUtilTest {
    @Test
    public void shouldUseForwardedSchemeIfPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("shr.com");
        request.setMethod("GET");
        request.setRequestURI("/patients/HID1/encounters");
        request.addHeader("X-Forwarded-Proto", "https");

        String url = UrlUtil.formRequestUrl(request);

        assertEquals("https://shr.com/patients/HID1/encounters", url);
    }

    @Test
    public void shouldSetLastUpdatedSinceIfGiven() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("shr.com");
        request.setMethod("GET");
        request.setRequestURI("/patients/HID1/encounters");

        String url = UrlUtil.formUrlAndAddLastUpdatedQueryParams(request, DateUtil.parseDate("2016-04-11"), null);

        assertTrue(url.contains("updatedSince"));
    }

    @Test
    public void shouldSetLastMarkerIfGiven() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("shr.com");
        request.setMethod("GET");
        request.setRequestURI("/patients/HID1/encounters");

        String url = UrlUtil.formUrlAndAddLastUpdatedQueryParams(request, null, "uuid-2011");

        assertTrue(url.contains("lastMarker"));
    }
}