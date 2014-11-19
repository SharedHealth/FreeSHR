package org.freeshr.interfaces.encounter.ws;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.domain.service.EncounterService;
import org.freeshr.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;
import rx.functions.Action1;
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
            @RequestBody EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        logger.debug("Create encounter. " + encounterBundle);
        encounterBundle.setHealthId(healthId);

        final DeferredResult<EncounterResponse> deferredResult = new DeferredResult<>();
        Observable<EncounterResponse> encounterResponse = encounterService.ensureCreated(encounterBundle);

        encounterResponse.subscribe(new Action1<EncounterResponse>() {
            @Override
            public void call(EncounterResponse encounterResponse) {
                if (encounterResponse.isSuccessful()) {
                    deferredResult.setResult(encounterResponse);
                } else {
                    if (encounterResponse.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition)) {
                        deferredResult.setErrorResult(new PreconditionFailed(encounterResponse));
                    } else {
                        deferredResult.setErrorResult(new UnProcessableEntity(encounterResponse));
                    }
                }
            }
        });

        return deferredResult;
    }

    @RequestMapping(value = "/patients/{healthId}/encounters", method = RequestMethod.GET)
    public DeferredResult<List<EncounterBundle>> findAll(@PathVariable String healthId) {
        logger.debug("Find all encounters by health id: " + healthId);
        final DeferredResult<List<EncounterBundle>> deferredResult = new DeferredResult<>();
        encounterService.findAll(healthId).subscribe(new Action1<List<EncounterBundle>>() {
            @Override
            public void call(List<EncounterBundle> encounterBundles) {
                deferredResult.setResult(encounterBundles);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                deferredResult.setErrorResult(throwable);
            }
        });

        return deferredResult;
    }

    @RequestMapping(value = "/catchments/{catchment}/encounters", method = RequestMethod.GET, produces = {"application/json", "application/atom+xml"})
    public DeferredResult<EncounterSearchResponse> findEncountersForCatchment(
            final HttpServletRequest request,
            @RequestHeader String facilityId,
            @PathVariable String catchment,
            @RequestParam(value = "updatedSince", required = false) String updatedSince,
            @RequestParam(value = "lastMarker", required = false) final String lastMarker)
            throws ExecutionException, InterruptedException, ParseException, UnsupportedEncodingException {
        logger.debug(String.format("Find all encounters for facility %s in catchment %s", facilityId, catchment));
        final DeferredResult<EncounterSearchResponse> deferredResult = new DeferredResult<>();
        final Date requestedDate = getRequestedDate(updatedSince);
        final Observable<List<EncounterBundle>> catchmentEncounters =
                findFacilityCatchmentEncounters(facilityId, catchment, lastMarker, requestedDate);

        catchmentEncounters.subscribe(new Action1<List<EncounterBundle>>() {
            @Override
            public void call(List<EncounterBundle> encounterBundles) {

                try {
                    EncounterSearchResponse searchResponse = new EncounterSearchResponse(
                            getRequestUri(request, requestedDate, lastMarker), encounterBundles);
                    searchResponse.setNavLinks(null, getNextResultURL(request, encounterBundles, requestedDate));

                    deferredResult.setResult(searchResponse);
                } catch (Throwable t) {
                    deferredResult.setErrorResult(t);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                deferredResult.setErrorResult(throwable);
            }
        });

        return deferredResult;
    }

    private Observable<List<EncounterBundle>> findFacilityCatchmentEncounters(String facilityId, String catchment,
                                                                              String lastMarker, Date lastUpdateDate) {
        int encounterFetchLimit = EncounterService.getEncounterFetchLimit();
        Observable<List<EncounterBundle>> facilityCatchmentEncounters =
                encounterService.findEncountersForFacilityCatchment(facilityId, catchment, lastUpdateDate, encounterFetchLimit * 2);

        return filterAfterMarker(facilityCatchmentEncounters, lastMarker, encounterFetchLimit);
    }

    /**
     * @param updatedSince
     * @return parsed date. For formats please refer to @see org.freeshr.utils.DateUtil#DATE_FORMATS
     * If no date is given, then by default, the date one month earlier is calculated.
     * @throws UnsupportedEncodingException
     */
    Date getRequestedDate(String updatedSince) throws UnsupportedEncodingException {
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
        Date lastUpdateDate = DateUtil.parseDate(updatedSince);
        return lastUpdateDate;
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
            String nextApplicableDate = String.format("%s-%s-01", requestedTime.get(Calendar.YEAR), requestedTime.get(Calendar.MONTH) + 1);
            return UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                    .queryParam("updatedSince", nextApplicableDate).build().toString();
        }
        return null;
    }

    String getRequestUri(HttpServletRequest request, Date lastUpdateDate, String lastMarker)
            throws UnsupportedEncodingException {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                        .queryParam("updatedSince", URLEncoder.encode(DateUtil.toISOString(lastUpdateDate), "UTF-8"));
        if (!StringUtils.isBlank(lastMarker)) {
            uriBuilder.queryParam("lastMarker", lastMarker);
        }
        return uriBuilder.build().toString();
    }


    private Observable<List<EncounterBundle>> filterAfterMarker(final Observable<List<EncounterBundle>> encounters, final String lastMarker, final int limit) {

        return encounters.map(new Func1<List<EncounterBundle>, List<EncounterBundle>>() {
            @Override
            public List<EncounterBundle> call(List<EncounterBundle> encounterBundles) {
                if (StringUtils.isBlank(lastMarker)) {
                    return encounterBundles.size() > limit ? encounterBundles.subList(0, limit) : encounterBundles;
                }

                int lastMarkerIndex = identifyLastMarker(lastMarker, encounterBundles);
                if (lastMarkerIndex >= 0) {
                    if ((lastMarkerIndex + 1) <= encounterBundles.size()) {
                        List<EncounterBundle> remainingEncounters = encounterBundles.subList(lastMarkerIndex + 1, encounterBundles.size());
                        return remainingEncounters.size() > limit ? remainingEncounters.subList(0, limit) : remainingEncounters;
                    }
                }
                return new ArrayList<EncounterBundle>();
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

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
    @ResponseBody
    @ExceptionHandler(PreconditionFailed.class)
    public EncounterResponse preConditionFailed(PreconditionFailed preconditionFailed) {
        return preconditionFailed.getResult();
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    @ExceptionHandler(UnProcessableEntity.class)
    public EncounterResponse unProcessableEntity(UnProcessableEntity unProcessableEntity) {
        return unProcessableEntity.getResult();
    }


}
