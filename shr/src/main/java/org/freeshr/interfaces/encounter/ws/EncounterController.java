package org.freeshr.interfaces.encounter.ws;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.application.fhir.EncounterResponse;
import org.freeshr.domain.service.EncounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/patients/{healthId}")
public class EncounterController {
    private static final Logger logger = LoggerFactory.getLogger(EncounterController.class);

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(value = "/encounters", method = RequestMethod.POST)
    public DeferredResult<EncounterResponse> create(@PathVariable String healthId, @RequestBody EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        logger.debug("Create encounter. " + encounterBundle);
        encounterBundle.setHealthId(healthId);

        final DeferredResult<EncounterResponse> deferredResult = new DeferredResult<>();
        encounterService.ensureCreated(encounterBundle).addCallback(new ListenableFutureCallback<EncounterResponse>() {
            @Override
            public void onSuccess(EncounterResponse result) {
                if (result.isSuccessful()) {
                    deferredResult.setResult(result);
                } else {
                    if (result.isTypeOfFailure(EncounterResponse.TypeOfFailure.Precondition)) {
                        deferredResult.setErrorResult(new PreconditionFailed(result));
                    } else {
                        deferredResult.setErrorResult(new UnProcessableEntity(result));
                    }
                }
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(error);
            }
        });
        return deferredResult;
    }

    @RequestMapping(value = "/encounters", method = RequestMethod.GET)
    public DeferredResult<List<EncounterBundle>> findAll(@PathVariable String healthId) {
        logger.debug("Find all encounters by health id: " + healthId);
        final DeferredResult<List<EncounterBundle>> deferredResult = new DeferredResult<List<EncounterBundle>>();

        encounterService.findAll(healthId).addCallback(new ListenableFutureCallback<List<EncounterBundle>>() {
            @Override
            public void onSuccess(List<EncounterBundle> result) {
                deferredResult.setResult(result);
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(error);
            }
        });
        return deferredResult;
    }

    @RequestMapping(value = "/encounters/bycatchments",method = RequestMethod.GET)
    public DeferredResult<List<EncounterBundle>> findAllByCatchment(@PathVariable String facilityId){
        logger.debug(" Find all encounters by facility id:" + facilityId);
        final DeferredResult<List<EncounterBundle>> deferredResult = new DeferredResult<>();

        encounterService.findAllEncountersByCatchments(facilityId).addCallback(new ListenableFutureCallback<List<EncounterBundle>>() {
            @Override
            public void onSuccess(List<EncounterBundle> result) {
                deferredResult.setResult(result);
            }

            @Override
            public void onFailure(Throwable error) {
                deferredResult.setErrorResult(error);
            }
        });
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
