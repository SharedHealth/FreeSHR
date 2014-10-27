package org.freeshr.interfaces.encounter.ws;


import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedOutput;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.service.EncounterService;
import org.freeshr.utils.DateUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
        List<EncounterBundle> dummyEncounters = createEncounterBundles("hid01", 50, DateUtil.parseDate("2014-10-10"));
        Mockito.when(mockedEncounterService.findEncountersForFacilityCatchment(anyString(), anyString(), any(Date.class),
                eq(EncounterService.DEFAULT_FETCH_LIMIT))).thenReturn(slice(EncounterService.DEFAULT_FETCH_LIMIT, dummyEncounters));

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(null, null, "/catchments/3026/encounters");
        DeferredResult<EncounterSearchResponse> encountersForCatchment = controller.findEncountersForCatchment(mockHttpServletRequest, "F1", "3026", "2014-10-10", null);
        EncounterSearchResponse response = (EncounterSearchResponse) encountersForCatchment.getResult();
        assertEquals(EncounterService.DEFAULT_FETCH_LIMIT, response.getEntries().size());
        assertEquals("http://localhost/catchments/3026/encounters?updatedSince=2014-10-10T00%3A00%3A04.000%2B0530&lastMarker=e-5", response.getNextUrl());


        encountersForCatchment = controller.findEncountersForCatchment(mockHttpServletRequest, "F1", "3026", "2014-10-10", "e-2");
        response = (EncounterSearchResponse) encountersForCatchment.getResult();
        assertEquals(3, response.getEntries().size());
        System.out.println(response.getNextUrl());
    }

    @Ignore
    @Test
    public void shouldGetAtomFeed() throws Exception {
        List<EncounterBundle> dummyEncounters = createEncounterBundles("hid01", 50, DateUtil.parseDate("2014-10-10"));
        Mockito.when(mockedEncounterService.findEncountersForFacilityCatchment(anyString(), anyString(), any(Date.class),
                eq(EncounterService.DEFAULT_FETCH_LIMIT))).thenReturn(slice(EncounterService.DEFAULT_FETCH_LIMIT, dummyEncounters));

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(null, null, "/catchments/3026/encounters");
        DeferredResult<EncounterSearchResponse> encountersForCatchment = controller.findEncountersForCatchment(mockHttpServletRequest, "F1", "3026", "2014-10-10", null);
        EncounterSearchResponse response = (EncounterSearchResponse) encountersForCatchment.getResult();
        List<EncounterBundle> results = response.getEntries();

        EncounterFeedHelper encounterFeedBuilder = new EncounterFeedHelper();
        Feed feed = encounterFeedBuilder.generateFeed(response, generateFeedId("2014-10-10", null));
        System.out.println(new WireFeedOutput().outputString(feed));
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
