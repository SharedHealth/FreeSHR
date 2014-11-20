package org.freeshr.interfaces.encounter.ws;


import com.sun.syndication.feed.atom.Feed;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.service.EncounterService;
import org.freeshr.utils.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class EncounterControllerTest {

    @Mock
    EncounterService mockedEncounterService;

    EncounterController controller;

    @Before
    public void setUp() {
        initMocks(this);
        controller = new EncounterController(mockedEncounterService);
    }

    @Test
    public void shouldGetPagedEncountersForCatchment() throws Exception {
        int encounterFetchLimit = EncounterService.getEncounterFetchLimit();
        List<EncounterBundle> dummyEncounters = createEncounterBundles("hid01", 50, DateUtil.parseDate("2014-10-10"));
        Mockito.when(mockedEncounterService.findEncountersForFacilityCatchment(anyString(), anyString(), any(Date.class),
                eq(encounterFetchLimit * 2))).thenReturn(Observable.just(slice(encounterFetchLimit*2, dummyEncounters)));

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(null, null, "/catchments/3026/encounters");
        DeferredResult<EncounterSearchResponse> encountersForCatchment = controller.findEncountersForCatchment(mockHttpServletRequest, "F1", "3026", "2014-10-10", null);
        EncounterSearchResponse response = (EncounterSearchResponse) encountersForCatchment.getResult();
        List<EncounterBundle> entries = response.getEntries();
        assertEquals(encounterFetchLimit, entries.size());
        String expectedNextUrl = response.getNextUrl();
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(expectedNextUrl), "UTF-8");
        assertEquals("e-20", params.get(1).getValue());


        encountersForCatchment = controller.findEncountersForCatchment(mockHttpServletRequest, "F1", "3026", "2014-10-10", "e-22");
        response = (EncounterSearchResponse) encountersForCatchment.getResult();
        entries = response.getEntries();
        assertEquals(18, entries.size());
    }

    @Test
    public void shouldGetAtomFeed() throws Exception {
        int encounterFetchLimit = EncounterService.getEncounterFetchLimit();
        List<EncounterBundle> dummyEncounters = createEncounterBundles("hid01", 50, DateUtil.parseDate("2014-10-10"));
        Mockito.when(mockedEncounterService.findEncountersForFacilityCatchment(anyString(), anyString(), any(Date.class),
                eq(encounterFetchLimit*2))).thenReturn(Observable.just(slice(encounterFetchLimit*2, dummyEncounters)));

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(null, null, "/catchments/3026/encounters");
        DeferredResult<EncounterSearchResponse> encountersForCatchment = controller.findEncountersForCatchment(mockHttpServletRequest, "F1", "3026", "2014-10-10", null);
        EncounterSearchResponse response = (EncounterSearchResponse) encountersForCatchment.getResult();
        List<EncounterBundle> results = response.getEntries();

        EncounterFeedHelper encounterFeedBuilder = new EncounterFeedHelper();
        Feed feed = encounterFeedBuilder.generateFeed(response, generateFeedId("2014-10-10", null));
        assertEquals(encounterFetchLimit, feed.getEntries().size());
    }

    @Test
    public void shouldRollOverForNextUrl() throws UnsupportedEncodingException, URISyntaxException {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(null, null, "/catchments/3026/encounters");
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, currentYear-1);
        String nextResultURL = controller.getNextResultURL(mockHttpServletRequest, new ArrayList<EncounterBundle>(), calendar.getTime());
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(nextResultURL), "UTF-8");

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String expected = new SimpleDateFormat("YYYY-MM-dd").format(calendar.getTime());
        assertTrue("Should have rolled over to next month: " + expected, params.get(0).getValue().startsWith(expected));

        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 1);
        nextResultURL = controller.getNextResultURL(mockHttpServletRequest, new ArrayList<EncounterBundle>(), futureDate.getTime());
        assertNull("For future year, should have returned null", nextResultURL);

        nextResultURL = controller.getNextResultURL(mockHttpServletRequest, new ArrayList<EncounterBundle>(), Calendar.getInstance().getTime());
        assertNull("For current year, should have returned null", nextResultURL);
    }

    @Test
    public void shouldDefaultToStartOfMonthIfNotSpecified() throws UnsupportedEncodingException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(controller.getRequestedDateForCatchment(""));
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private String generateFeedId(String updatedSince, String requestedMarker) {
        return StringUtils.isBlank(requestedMarker) ? "E-" + updatedSince : "E-" + updatedSince + "%2B" + requestedMarker;
    }

    private List<EncounterBundle> slice(int size, List<EncounterBundle> dummyEncounters) {
        return dummyEncounters.subList(0, size);
    }


    private List<EncounterBundle> createEncounterBundles(String healthId, int size, Date startingFrom) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startingFrom);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);

        List<EncounterBundle> encounters = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            EncounterBundle encounter = new EncounterBundle();
            encounter.setEncounterId("e-" + (i + 1));
            encounter.setHealthId(healthId);
            calendar.set(Calendar.SECOND, i);
            encounter.setReceivedDate(DateUtil.toISOString(calendar.getTime()));
            encounter.setEncounterContent("content-" + (i+1));
            encounters.add(encounter);
        }
        return encounters;
    }
}
