package org.freeshr.interfaces.encounter.ws;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.service.EncounterService;
import org.freeshr.application.fhir.InvalidEncounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/patients/{healthId}")
public class EncounterController {
    private static final Logger logger = LoggerFactory.getLogger(EncounterController.class);

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(value = "/encounters", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    public DeferredResult<String> create(@PathVariable String healthId, @RequestBody EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        encounterBundle.setHealthId(healthId);
        logger.debug("Create encounter. " + encounterBundle);

        final DeferredResult<String> deferredResult = new DeferredResult<String>();
        try{
            encounterService.ensureCreated(encounterBundle).addCallback(new ListenableFutureCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    deferredResult.setResult(result);
                }

                @Override
                public void onFailure(Throwable error) {
                    deferredResult.setErrorResult(error);
                }
            });
        } catch(InvalidEncounter e){
            deferredResult.setErrorResult(e.getError());
        }
        return deferredResult;
    }

    @RequestMapping(value = "/encounters", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
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
}
