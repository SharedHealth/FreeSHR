package org.freeshr.interfaces.encounter.ws;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.domain.service.EncounterService;
import org.freeshr.infrastructure.security.UserInfo;
import org.freeshr.interfaces.encounter.ws.exceptions.*;
import org.freeshr.utils.CollectionUtils;
import org.freeshr.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.freeshr.infrastructure.security.AccessFilter.*;
import static org.freeshr.utils.HttpUtil.*;

@RestController
public class EncounterController {
    private static final Logger logger = LoggerFactory.getLogger(EncounterController.class);

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(value = "/patients/{healthId}/encounters", method = RequestMethod.POST)
    public DeferredResult<EncounterResponse> create(
            @PathVariable String healthId,
            @RequestBody EncounterBundle encounterBundle,
            HttpServletRequest request) throws ExecutionException, InterruptedException {
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Create encounter request for patient (healthId) %s", healthId));
        final DeferredResult<EncounterResponse> deferredResult = new DeferredResult<>();

        try {
            logger.debug("Create encounter. " + encounterBundle.getContent());
            encounterBundle.setHealthId(healthId);
            validateAccessToSaveEncounter(userInfo);
            Observable<EncounterResponse> encounterResponse = encounterService.ensureCreated(encounterBundle,
                    request.getHeader(CLIENT_ID_KEY), request.getHeader(FROM_KEY), request.getHeader(AUTH_TOKEN_KEY));

            encounterResponse.subscribe(new Action1<EncounterResponse>() {
                @Override
                public void call(EncounterResponse encounterResponse) {
                    if (encounterResponse.isSuccessful()) {
                        logger.debug(encounterResponse.toString());
                        deferredResult.setResult(encounterResponse);
                    } else {
                        //TODO: move code to encounter response class
                        RuntimeException errorResult = encounterResponse.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition) ?
                                new PreconditionFailed(encounterResponse)
                                : new UnProcessableEntity(encounterResponse);
                        logger.error(errorResult.getMessage());
                        deferredResult.setErrorResult(errorResult);
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable error) {
                    logger.error(error.getMessage());
                    deferredResult.setErrorResult(error);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }


    @RequestMapping(value = "/patients/{healthId}/encounters", method = RequestMethod.GET,
            produces = {"application/json", "application/atom+xml"})
    public DeferredResult<EncounterSearchResponse> findEncountersForPatient(
            final HttpServletRequest request,
            @PathVariable String healthId,
            @RequestParam(value = "updatedSince", required = false) String updatedSince) {
        logger.debug("Find all encounters by health id: " + healthId);
        final UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, String.format("Find all encounters of patient (healthId) %s", healthId));
        final DeferredResult<EncounterSearchResponse> deferredResult = new DeferredResult<>();

        try {
            final boolean isRestrictedAccess = isAccessRestrictedToEncounterFetchForPatient(healthId, userInfo);
            final Date requestedDate = getRequestedDate(updatedSince);
            Observable<List<EncounterBundle>> encountersForPatient =
                    encounterService.findEncountersForPatient(healthId, requestedDate, 200);
            encountersForPatient.subscribe(new Action1<List<EncounterBundle>>() {
                @Override
                public void call(List<EncounterBundle> encounterBundles) {
                    try {
                        encounterBundles = filterEncounters(isRestrictedAccess, encounterBundles);
                        EncounterSearchResponse searchResponse = new EncounterSearchResponse(
                                getRequestUri(request, requestedDate, null), encounterBundles);
                        logger.debug(searchResponse.toString());
                        deferredResult.setResult(searchResponse);
                    } catch (UnsupportedEncodingException e) {
                        logger.error(e.getMessage());
                        deferredResult.setErrorResult(e);
                    }

                }


            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    logger.error(throwable.getMessage());
                    deferredResult.setErrorResult(throwable);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }

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
        logger.debug(format("Find all encounters for facility %s in catchment %s", userInfo.getFacilityId(), catchment));
        logAccessDetails(userInfo, format("Find all encounters for facility %s in catchment %s", userInfo.getFacilityId(), catchment));
        try {
            if (catchment.length() < 4) {
                throw new BadRequest("Catchment should have division and district");
            }
            final Date requestedDate = getRequestedDateForCatchment(updatedSince);
            final boolean isRestrictedAccess = isAccessRestrictedToEncounterFetchForCatchment(catchment, userInfo);
            final Observable<List<EncounterBundle>> catchmentEncounters =
                    findFacilityCatchmentEncounters(catchment, lastMarker, requestedDate);
            catchmentEncounters.subscribe(new Action1<List<EncounterBundle>>() {
                @Override
                public void call(List<EncounterBundle> encounterBundles) {
                    try {
                        encounterBundles = filterEncounters(isRestrictedAccess, encounterBundles);
                        EncounterSearchResponse searchResponse = new EncounterSearchResponse(
                                getRequestUri(request, requestedDate, lastMarker), encounterBundles);
                        searchResponse.setNavLinks(null, getNextResultURL(request, encounterBundles, requestedDate));
                        logger.debug(searchResponse.toString());
                        deferredResult.setResult(searchResponse);
                    } catch (Throwable t) {
                        logger.error(t.getMessage());
                        deferredResult.setErrorResult(t);
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    logger.error(throwable.getMessage());
                    deferredResult.setErrorResult(throwable);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }

    @RequestMapping(value = "/patients/{healthId}/encounters/{encounterId}", method = RequestMethod.GET,
            produces = {"application/json", "application/xml"})
    public DeferredResult<EncounterBundle> findEncountersForPatient(
            @PathVariable String healthId, @PathVariable final String encounterId) {
        final DeferredResult<EncounterBundle> deferredResult = new DeferredResult<>();
        logger.debug(format("Find encounter %s for patient %s", encounterId, healthId));
        UserInfo userInfo = getUserInfo();
        logAccessDetails(userInfo, format("Find encounter %s for patient %s", encounterId, healthId));

        try {
            final boolean isRestrictedAccess = isAccessRestrictedToEncounterFetchForPatient(healthId, userInfo);
            Observable<EncounterBundle> observable = encounterService.findEncounter(healthId,
                    encounterId).firstOrDefault(null);
            observable.subscribe(new Action1<EncounterBundle>() {
                                     @Override
                                     public void call(EncounterBundle encounterBundle) {
                                         if (encounterBundle != null) {
                                             logger.debug(encounterBundle.toString());
                                             if (CollectionUtils.isEmpty(filterEncounters(isRestrictedAccess, asList(encounterBundle)))) {
                                                 Forbidden errorResult = new Forbidden(format("Access for encounter %s for is denied",
                                                         encounterId));
                                                 logger.error(errorResult.getMessage());
                                                 deferredResult.setErrorResult(errorResult);
                                             } else {
                                                 deferredResult.setResult(encounterBundle);
                                             }
                                         } else {
                                             String errorMessage = format("Encounter %s not found", encounterId);
                                             logger.error(errorMessage);
                                             deferredResult.setErrorResult(new ResourceNotFound(errorMessage));
                                         }
                                     }
                                 },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            logger.error(throwable.getMessage());
                            deferredResult.setErrorResult(throwable);
                        }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage());
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }


    private Observable<List<EncounterBundle>> findFacilityCatchmentEncounters(String catchment,
                                                                              String lastMarker, Date lastUpdateDate) {
        int encounterFetchLimit = EncounterService.getEncounterFetchLimit();
        Observable<List<EncounterBundle>> facilityCatchmentEncounters =
                encounterService.findEncountersForFacilityCatchment(catchment, lastUpdateDate,
                        encounterFetchLimit * 2);

        return filterAfterMarker(facilityCatchmentEncounters, lastMarker, encounterFetchLimit);
    }

    /**
     * @param updatedSince
     * @return parsed date. For formats please refer to @see org.freeshr.utils.DateUtil#DATE_FORMATS
     * If no date is given, then by default, beginning of month is considered.
     * @throws UnsupportedEncodingException
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

    String getNextResultURL(
            HttpServletRequest request, List<EncounterBundle> requestResults, Date requestedDate)
            throws UnsupportedEncodingException, URISyntaxException {
        int size = requestResults.size();
        if (size <= 0) {
            //next result set url might need to rolled over
            return rollingFeedUrl(request, requestedDate);
        }

        EncounterBundle lastEncounter = requestResults.get(size - 1);
        String lastEncounterDate = URLEncoder.encode(lastEncounter.getReceivedDate(), "UTF-8");

        return UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                .queryParam("updatedSince", lastEncounterDate)
                .queryParam("lastMarker", lastEncounter.getEncounterId())
                .build().toString();
    }

    String rollingFeedUrl(HttpServletRequest request, Date forDate) throws UnsupportedEncodingException {
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

    String getRequestUri(HttpServletRequest request, Date lastUpdateDate, String lastMarker)
            throws UnsupportedEncodingException {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(request.getRequestURL().toString());
        if (lastUpdateDate != null) {
            uriBuilder.queryParam("updatedSince", URLEncoder.encode(DateUtil.toISOString(lastUpdateDate), "UTF-8"));
        }
        if (!StringUtils.isBlank(lastMarker)) {
            uriBuilder.queryParam("lastMarker", lastMarker);
        }
        return uriBuilder.build().toString();
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
                logger.error(throwable.getMessage());
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


    private UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void logAccessDetails(UserInfo userInfo, String action) {
        logger.info(String.format("ACCESS: USER=%s TYPE=%s ACTION=%s", userInfo.getId(), userInfo.getName(), action));
    }

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    @ExceptionHandler(PreconditionFailed.class)
    public EncounterResponse preConditionFailed(PreconditionFailed preconditionFailed) {
        return preconditionFailed.getResult();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(BadRequest.class)
    public ErrorInfo badRequest(BadRequest badRequest) {
        return new ErrorInfo(HttpStatus.BAD_REQUEST, badRequest.getMessage());
    }


    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    @ExceptionHandler(UnProcessableEntity.class)
    public EncounterResponse unProcessableEntity(UnProcessableEntity unProcessableEntity) {
        return unProcessableEntity.getResult();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    @ExceptionHandler(ResourceNotFound.class)
    public ErrorInfo resourceNotFound(ResourceNotFound resourceNotFound) {
        return new ErrorInfo(HttpStatus.NOT_FOUND, resourceNotFound.getMessage());
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    @ExceptionHandler(Unauthorized.class)
    public ErrorInfo unauthorized(Unauthorized unauthorized) {
        return new ErrorInfo(HttpStatus.UNAUTHORIZED, unauthorized.getMessage());
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    @ExceptionHandler(Forbidden.class)
    public ErrorInfo forbidden(Forbidden forbidden) {
        return new ErrorInfo(HttpStatus.FORBIDDEN, forbidden.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ErrorInfo catchAll(Exception exception) {
        return new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, exception.getLocalizedMessage());
    }
}
