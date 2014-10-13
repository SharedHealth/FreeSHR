package org.freeshr.interfaces.encounter.ws;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.domain.service.EncounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.text.ParseException;
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
    public DeferredResult<EncounterResponse> create(@PathVariable String healthId, @RequestBody EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
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


    @RequestMapping(value = "/encounters/bycatchments", method = RequestMethod.GET)
    public DeferredResult<List<EncounterBundle>> findAllByCatchment(@RequestHeader String facilityId, @RequestParam(value = "facilityDate",required = false) String facilityDate) throws ExecutionException, InterruptedException, ParseException {
        logger.debug(" Find all encounters by facility id:" + facilityId);
        final DeferredResult<List<EncounterBundle>> deferredResult = new DeferredResult<>();
        try {
            List<EncounterBundle> encountersByCatchments = encounterService.findEncountersByCatchments(facilityId, facilityDate);
            deferredResult.setResult(encountersByCatchments);
        }
        catch (Exception e){
            deferredResult.setErrorResult(e);
        }
        return deferredResult;

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
