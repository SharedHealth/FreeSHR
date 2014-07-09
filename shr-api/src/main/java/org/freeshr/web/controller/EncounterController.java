package org.freeshr.web.controller;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.service.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/encounter")
public class EncounterController {

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {APPLICATION_JSON_VALUE})
    public DeferredResult<String> create(@RequestBody EncounterBundle encounterBundle) throws ExecutionException, InterruptedException {
        final DeferredResult<String> deferredResult = new DeferredResult<String>();
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
        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.GET)
    public DeferredResult<EncounterBundle> findByHealthId(@RequestParam(value = "hid") String healthId) {
        final DeferredResult<EncounterBundle> deferredResult = new DeferredResult<EncounterBundle>();

        encounterService.findByHealthId(healthId).addCallback(new ListenableFutureCallback<EncounterBundle>() {
            @Override
            public void onSuccess(EncounterBundle result) {
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
