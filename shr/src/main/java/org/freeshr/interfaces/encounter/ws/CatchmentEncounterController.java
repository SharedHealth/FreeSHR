package org.freeshr.interfaces.encounter.ws;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.domain.service.CatchmentEncounterService;
import org.freeshr.events.EncounterEvent;
import org.freeshr.infrastructure.security.AccessFilter;
import org.freeshr.infrastructure.security.ConfidentialEncounterHandler;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.interfaces.encounter.ws.exceptions.BadRequest;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;
import rx.functions.Action1;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;

@RestController
public class CatchmentEncounterController extends ShrController {
    private static final Logger logger = LoggerFactory.getLogger(CatchmentEncounterController.class);

    private CatchmentEncounterService catchmentEncounterService;
    private AccessFilter accessFilter;
    private ConfidentialEncounterHandler confidentialEncounterHandler;

    @Autowired
    public CatchmentEncounterController(CatchmentEncounterService catchmentEncounterService, ConfidentialEncounterHandler confidentialEncounterHandler) {
        this.catchmentEncounterService = catchmentEncounterService;
        this.accessFilter = new AccessFilter();
        this.confidentialEncounterHandler = confidentialEncounterHandler;
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR_FACILITY', 'ROLE_SHR_PROVIDER', 'ROLE_SHR System Admin')")
    @RequestMapping(value = "/catchments/{catchment}/encounters", method = RequestMethod.GET,
            produces = {"application/json", "application/atom+xml"})
    public DeferredResult<EncounterSearchResponse> findEncounterFeedForCatchment(
            final HttpServletRequest request,
            @PathVariable String catchment,
            @RequestParam(value = "updatedSince", required = false) String updatedSince,
            @RequestParam(value = "lastMarker", required = false) final String lastMarker)
            throws ExecutionException, InterruptedException, ParseException, UnsupportedEncodingException {
        final DeferredResult<EncounterSearchResponse> deferredResult = new DeferredResult<>();
        final UserInfo userInfo = getUserInfo();
        logger.debug(format("Find all encounters for facility %s in catchment %s", userInfo.getProperties().getFacilityId(), catchment));
        logAccessDetails(userInfo, format("Find all encounters for facility %s in catchment %s", userInfo.getProperties().getFacilityId(), catchment));
        try {
            if (catchment.length() < 4) {
                deferredResult.setErrorResult(new BadRequest("Catchment should have division and district"));
                return deferredResult;
            }
            final Date requestedDate = getRequestedDateForCatchment(updatedSince);
            logger.info(String.format("fetching catching encounters for [catchment=%s], [date=%s], [marker=%s]",
                    catchment, DateUtil.toDateString(requestedDate, DateUtil.ISO_DATE_IN_MILLIS_FORMAT), lastMarker));

            final Boolean isUserAccessRestrictedForConfidentialData = accessFilter.isAccessRestrictedToEncounterFetchForCatchment(catchment, userInfo);
            if (isUserAccessRestrictedForConfidentialData == null) {
                deferredResult.setErrorResult(new Forbidden(String.format("Access is denied to user %s for catchment %s", userInfo.getProperties().getId(), catchment)));
                return deferredResult;
            }
            final Observable<List<EncounterEvent>> catchmentEncounters = catchmentEncounterService.findEncounterFeedForFacilityCatchment(catchment, requestedDate, lastMarker);

            catchmentEncounters.subscribe(new Action1<List<EncounterEvent>>() {
                @Override
                public void call(List<EncounterEvent> encounterEvents) {
                    try {
                        if (isUserAccessRestrictedForConfidentialData) {
                            encounterEvents = confidentialEncounterHandler.replaceConfidentialEncounterEvents(encounterEvents);
                        }
                        EncounterSearchResponse searchResponse = new EncounterSearchResponse(
                                UrlUtil.addLastUpdatedQueryParams(request, requestedDate, lastMarker), encounterEvents);
                        searchResponse.setNavLinks(null, getNextResultURL(request, encounterEvents, requestedDate));
                        logger.debug(searchResponse.toString());
                        deferredResult.setResult(searchResponse);
                    } catch (Throwable throwable) {
                        logger.debug(throwable.getMessage());
                        deferredResult.setErrorResult(throwable);
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    logger.debug(throwable.getMessage());
                    deferredResult.setErrorResult(throwable);
                }
            });
        } catch (Exception e) {
            logger.debug(e.getMessage());
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }

    /**
     * @param updatedSince
     * @return parsed date. For formats please refer to @see org.freeshr.utils.DateUtil#DATE_FORMATS
     * If no date is given, then by default, beginning of month is considered.
     * @throws java.io.UnsupportedEncodingException
     */
    Date getRequestedDateForCatchment(String updatedSince) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(updatedSince)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        }

        //NO need to decode the date, since the spring request mapper would have already decoded the string
        //String decodeLastUpdate = URLDecoder.decode(updatedSince, "UTF-8");
        return getRequestedDate(updatedSince);
    }

    private Date getRequestedDate(String updatedSince) {
        return StringUtils.isBlank(updatedSince) ? null : DateUtil.parseDate(updatedSince);
    }

    public String getNextResultURL(
            HttpServletRequest request, List<EncounterEvent> requestResults, Date requestedDate)
            throws UnsupportedEncodingException, URISyntaxException {
        int size = requestResults.size();
        if (size <= 0) {
            //next result set url might need to rolled over
            return rollingFeedUrl(request, requestedDate);
        }

        if (size == 1) {
            return null;
        }

        EncounterEvent lastEncounterEvent = requestResults.get(size - 1);
        return UrlUtil.addLastUpdatedQueryParams(request, lastEncounterEvent.getUpdatedAt(), lastEncounterEvent.getId());
    }

    private String rollingFeedUrl(HttpServletRequest request, Date forDate) throws UnsupportedEncodingException {
        Calendar requestedTime = Calendar.getInstance();
        requestedTime.setTime(forDate);
        int requestedYear = requestedTime.get(Calendar.YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        if (currentYear == requestedYear) return null; //same year
        if (currentYear < requestedYear) return null;  //future year
        if (currentYear > requestedYear) { //advance to the next month's beginning date.
            requestedTime.add(Calendar.MONTH, 1);

            String nextApplicableDate = format("%s-%02d-01", requestedTime.get(Calendar.YEAR),
                    requestedTime.get(Calendar.MONTH) + 1);
            return UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                    .queryParam("updatedSince", nextApplicableDate).build().toString();
        }
        return null;
    }

}
