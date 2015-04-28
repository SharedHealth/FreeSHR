package org.freeshr.interfaces.encounter.ws;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.service.CatchmentEncounterService;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.interfaces.encounter.ws.exceptions.BadRequest;
import org.freeshr.interfaces.encounter.ws.exceptions.Forbidden;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static org.freeshr.infrastructure.security.AccessFilter.filterEncounters;
import static org.freeshr.infrastructure.security.AccessFilter.isAccessRestrictedToEncounterFetchForCatchment;

@RestController
public class CatchmentEncounterController extends ShrController {
    private static final Logger logger = LoggerFactory.getLogger(CatchmentEncounterController.class);

    private CatchmentEncounterService catchmentEncounterService;

    @Autowired
    public CatchmentEncounterController(CatchmentEncounterService catchmentEncounterService) {
        this.catchmentEncounterService = catchmentEncounterService;
    }

    @PreAuthorize("hasAnyRole('ROLE_SHR_FACILITY', 'ROLE_SHR_PROVIDER', 'ROLE_SHR System Admin')")
    @RequestMapping(value = "/catchments/{catchment}/encounters", method = RequestMethod.GET,
            produces = {"application/json", "application/atom+xml"})
    public DeferredResult<EncounterSearchResponse> findEncountersForCatchment(
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
            final Boolean isRestrictedAccess = isAccessRestrictedToEncounterFetchForCatchment(catchment, userInfo);
            if (isRestrictedAccess == null) {
                deferredResult.setErrorResult(new Forbidden(String.format("Access is denied to user %s for catchment %s", userInfo.getProperties().getId(), catchment)));
                return deferredResult;
            }
            final Observable<List<EncounterBundle>> catchmentEncounters =
                    findFacilityCatchmentEncounters(catchment, lastMarker, requestedDate);
            catchmentEncounters.subscribe(new Action1<List<EncounterBundle>>() {
                @Override
                public void call(List<EncounterBundle> encounterBundles) {
                    try {
                        encounterBundles = filterEncounters(isRestrictedAccess, encounterBundles);
                        EncounterSearchResponse searchResponse = new EncounterSearchResponse(
                                UrlUtil.addLastUpdatedQueryParams(request, requestedDate, lastMarker), encounterBundles);
                        searchResponse.setNavLinks(null, getNextResultURL(request, encounterBundles, requestedDate));
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

    private Observable<List<EncounterBundle>> findFacilityCatchmentEncounters(String catchment,
                                                                              String lastMarker, Date lastUpdateDate) {
        int encounterFetchLimit = catchmentEncounterService.getEncounterFetchLimit();
        Observable<List<EncounterBundle>> facilityCatchmentEncounters =
                catchmentEncounterService.findEncountersForFacilityCatchment(catchment, lastUpdateDate,
                        encounterFetchLimit * 2);

        return filterAfterMarker(facilityCatchmentEncounters, lastMarker, encounterFetchLimit);
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
            HttpServletRequest request, List<EncounterBundle> requestResults, Date requestedDate)
            throws UnsupportedEncodingException, URISyntaxException {
        int size = requestResults.size();
        if (size <= 0) {
            //next result set url might need to rolled over
            return rollingFeedUrl(request, requestedDate);
        }

        EncounterBundle lastEncounter = requestResults.get(size - 1);
        return UrlUtil.addLastUpdatedQueryParams(request, lastEncounter.getReceivedAt(), lastEncounter.getEncounterId());
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

    private Observable<List<EncounterBundle>> filterAfterMarker(final Observable<List<EncounterBundle>> encounters,
                                                                final String lastMarker, final int limit) {

        return encounters.flatMap(new Func1<List<EncounterBundle>, Observable<? extends List<EncounterBundle>>>() {
            @Override
            public Observable<? extends List<EncounterBundle>> call(List<EncounterBundle> encounterBundles) {
                if (StringUtils.isBlank(lastMarker)) {
                    return Observable.just(encounterBundles.size() > limit ? encounterBundles.subList(0, limit) :
                            encounterBundles);
                }

                int lastMarkerIndex = identifyLastMarker(lastMarker, encounterBundles);
                if (lastMarkerIndex >= 0) {
                    if ((lastMarkerIndex + 1) <= encounterBundles.size()) {
                        List<EncounterBundle> remainingEncounters = encounterBundles.subList(lastMarkerIndex + 1,
                                encounterBundles.size());
                        return Observable.just(remainingEncounters.size() > limit ? remainingEncounters.subList(0,
                                limit) : remainingEncounters);
                    }
                }
                return Observable.just(new ArrayList<EncounterBundle>());
            }
        }, new Func1<Throwable, Observable<? extends List<EncounterBundle>>>() {
            @Override
            public Observable<? extends List<EncounterBundle>> call(Throwable throwable) {
                return Observable.error(throwable);
            }
        }, new Func0<Observable<? extends List<EncounterBundle>>>() {
            @Override
            public Observable<? extends List<EncounterBundle>> call() {
                return null;
            }
        });

    }

    private int identifyLastMarker(String lastMarker, final List<EncounterBundle> encountersByCatchment) {
        int idx = 0;
        for (EncounterBundle encounterBundle : encountersByCatchment) {
            if (encounterBundle.getEncounterId().equals(lastMarker)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }
}
