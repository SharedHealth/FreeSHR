package org.freeshr.interfaces.encounter.ws;


import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedOutput;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.service.EncounterService;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.atomfeed.AtomFeedHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.async.DeferredResult;

import java.net.URI;
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
        assertEquals(EncounterService.DEFAULT_FETCH_LIMIT, response.getResults().size());
        assertEquals("http://localhost/catchments/3026/encounters?updatedSince=2014-10-10T00%3A00%3A19.000%2B0530&lastMarker=e-20", response.getNextUrl());


        encountersForCatchment = controller.findEncountersForCatchment(mockHttpServletRequest, "F1", "3026", "2014-10-10", "e-11");
        response = (EncounterSearchResponse) encountersForCatchment.getResult();
        assertEquals(9, response.getResults().size());
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
        List<EncounterBundle> results = response.getResults();

        AtomFeedHelper atomFeedHelper = new AtomFeedHelper();
        String feedId = "3026-" + "2014-10-10";
        Feed feed = atomFeedHelper.generateFeed(new URI(mockHttpServletRequest.getRequestURL().toString()),
                results.subList(0,2), feedId, new AtomFeedHelper.NavigationLink(response.getPrevUrl(), response.getNextUrl()));
        System.out.println(new WireFeedOutput().outputString(feed));
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
            encounter.setEncounterId("e-" + (i+1));
            encounter.setHealthId(healthId);
            calendar.set(Calendar.SECOND, i);
            encounter.setReceivedDate(DateUtil.toISOString(calendar.getTime()));
            encounter.setEncounterContent("content-" + (i+1));
            encounters.add(encounter);
        }
        return encounters;
    }
}
