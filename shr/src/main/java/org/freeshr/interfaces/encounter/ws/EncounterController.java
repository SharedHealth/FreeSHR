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

    @RequestMapping(value = "/catchments/{catchment}/encounters", method = RequestMethod.GET)
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
            Date lastUpdateDate = getLastUpdateDate(updatedSince);
            List<EncounterBundle> catchmentEncounters =
               filterAfterMarker(
                       encounterService.findEncountersForFacilityCatchment(
                               facilityId, catchment, lastUpdateDate, EncounterService.DEFAULT_FETCH_LIMIT),
                       lastMarker, EncounterService.DEFAULT_FETCH_LIMIT);
            deferredResult.setResult(
               new EncounterSearchResponse(null,
                  getNextResultURL(request, catchmentEncounters), catchmentEncounters));
        }
        catch (Exception e){
            deferredResult.setErrorResult(e);
        }
        return deferredResult;
    }

    /**
     *
     * @param updatedSince
     * @return parsed date. For formats please refer to @see org.freeshr.utils.DateUtil#DATE_FORMATS
     * If no date is given, then by default, the date one month earlier is calculated.
     * @throws UnsupportedEncodingException
     */
    private Date getLastUpdateDate(String updatedSince) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(updatedSince)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        }
        String decodeLastUpdate = URLDecoder.decode(updatedSince, "UTF-8");
        Date lastUpdateDate = DateUtil.parseDate(decodeLastUpdate);
        return lastUpdateDate;
    }

    private String getNextResultURL(HttpServletRequest request, List<EncounterBundle> catchmentEncounters)
            throws UnsupportedEncodingException, URISyntaxException {
        int size = catchmentEncounters.size();
        if (size <= 0) return null;

        EncounterBundle lastEncounter = catchmentEncounters.get(size - 1);
        String receivedDate = URLEncoder.encode(lastEncounter.getReceivedDate(), "UTF-8");

        return UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                .queryParam("updatedSince", receivedDate)
                .queryParam("lastMarker", lastEncounter.getEncounterId()).build().toString();
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
