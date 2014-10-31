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

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;
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
        EncounterResponse encounterResponse = encounterService.ensureCreated(encounterBundle);
        if (encounterResponse.isSuccessful()) {
            deferredResult.setResult(encounterResponse);
        } else {
            if (encounterResponse.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition)) {
                deferredResult.setErrorResult(new PreconditionFailed(encounterResponse));
            } else {
                deferredResult.setErrorResult(new UnProcessableEntity(encounterResponse));
            }
        }
        return deferredResult;
    }

    @RequestMapping(value = "/patients/{healthId}/encounters", method = RequestMethod.GET)
    public DeferredResult<List<EncounterBundle>> findAll(@PathVariable String healthId) {
        logger.debug("Find all encounters by health id: " + healthId);
        final DeferredResult<List<EncounterBundle>> deferredResult = new DeferredResult<List<EncounterBundle>>();
        try {
            List<EncounterBundle> encounterBundles = encounterService.findAll(healthId);
            deferredResult.setResult(encounterBundles);
        }
        catch (Exception e){
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }

    /**
     *
     * @param facilityId
     * @param updatedSince
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws ParseException
     *
     * @deprecated do not use this method
     */
    @RequestMapping(value = "/encounters/bycatchments", method = RequestMethod.GET)
    public DeferredResult<List<EncounterBundle>> findAllByCatchment(
            @RequestHeader String facilityId,
            @RequestParam(value = "updatedSince",required = false) String updatedSince)
            throws ExecutionException, InterruptedException, ParseException {
        logger.debug(" Find all encounters by facility id:" + facilityId);
        final DeferredResult<List<EncounterBundle>> deferredResult = new DeferredResult<>();
        try {
            List<EncounterBundle> encountersByCatchments = encounterService.findAllEncountersByFacilityCatchments(facilityId, updatedSince);
            deferredResult.setResult(encountersByCatchments);
        }
        catch (Exception e){
            deferredResult.setErrorResult(e);
        }
        return deferredResult;

    }

    @RequestMapping(value = "/catchments/{catchment}/encounters", method = RequestMethod.GET, produces={"application/json", "application/atom+xml"})
    public DeferredResult<EncounterSearchResponse> findEncountersForCatchment(
            HttpServletRequest request,
            @RequestHeader String facilityId,
            @PathVariable String catchment,
            @RequestParam(value = "updatedSince",required = false) String updatedSince,
            @RequestParam(value = "lastMarker",  required = false) String lastMarker)
            throws ExecutionException, InterruptedException, ParseException {
        logger.debug(String.format("Find all encounters for facility %s in catchment %s", facilityId, catchment));
        final DeferredResult<EncounterSearchResponse> deferredResult = new DeferredResult<>();
        try {
            Date requestedDate = getRequestedDate(updatedSince);
            List<EncounterBundle> catchmentEncounters = findFacilityCatchmentEncounters(facilityId, catchment, lastMarker, requestedDate);
            EncounterSearchResponse searchResponse = new EncounterSearchResponse(
                    getRequestUri(request, requestedDate, lastMarker), catchmentEncounters);
            searchResponse.setNavLinks(null, getNextResultURL(request, catchmentEncounters));
            deferredResult.setResult(searchResponse);
        }
        catch (Exception e){
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }

    private List<EncounterBundle> findFacilityCatchmentEncounters(String facilityId, String catchment, String lastMarker, Date lastUpdateDate) throws ExecutionException, InterruptedException {
        List<EncounterBundle> facilityCatchmentEncounters =
           encounterService.findEncountersForFacilityCatchment(facilityId,catchment,lastUpdateDate, EncounterService.getEncounterFetchLimit());
        return filterAfterMarker( facilityCatchmentEncounters, lastMarker, EncounterService.getEncounterFetchLimit());
    }

    /**
     *
     * @param updatedSince
     * @return parsed date. For formats please refer to @see org.freeshr.utils.DateUtil#DATE_FORMATS
     * If no date is given, then by default, the date one month earlier is calculated.
     * @throws UnsupportedEncodingException
     */
    private Date getRequestedDate(String updatedSince) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(updatedSince)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.HOUR, 0);
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

    private String getNextResultURL(HttpServletRequest request, List<EncounterBundle> catchmentEncounters)
            throws UnsupportedEncodingException, URISyntaxException {
        int size = catchmentEncounters.size();
        if (size <= 0) return null;

        EncounterBundle lastEncounter = catchmentEncounters.get(size - 1);
        String lastEncounterDate = URLEncoder.encode(lastEncounter.getReceivedDate(), "UTF-8");

        return UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                .queryParam("updatedSince", lastEncounterDate)
                .queryParam("lastMarker", lastEncounter.getEncounterId())
                .build().toString();
    }

    private String getRequestUri(HttpServletRequest request, Date lastUpdateDate, String lastMarker)
            throws UnsupportedEncodingException {
        UriComponentsBuilder uriBuilder =
           UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
              .queryParam("updatedSince", URLEncoder.encode(DateUtil.toISOString(lastUpdateDate), "UTF-8"));
        if (!StringUtils.isBlank(lastMarker)) {
            uriBuilder.queryParam("lastMarker", lastMarker);
        }
        return uriBuilder.build().toString();
    }


    private List<EncounterBundle> filterAfterMarker(List<EncounterBundle> encounters, String lastMarker, int limit) {
        if (StringUtils.isBlank(lastMarker)) return encounters;

        //TODO use a linkedHashSet
        int lastMarkerIndex = identifyLastMarker(lastMarker, encounters);
        if (lastMarkerIndex >= 0) {
            if ((lastMarkerIndex+1) <= encounters.size()) {
                return encounters.subList(lastMarkerIndex + 1, encounters.size());
            }
        }
        return new ArrayList<>();
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
