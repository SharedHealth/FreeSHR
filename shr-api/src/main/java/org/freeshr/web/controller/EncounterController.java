package org.freeshr.web.controller;

import org.freeshr.domain.model.encounter.Encounter;
import org.freeshr.domain.service.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/encounter")
public class EncounterController {

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public DeferredResult<Boolean> create(@RequestBody Encounter encounter) throws ExecutionException, InterruptedException {
        final DeferredResult<Boolean> deferredResult = new DeferredResult<Boolean>();
        encounterService.ensureCreated(encounter).addCallback(new ListenableFutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
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
