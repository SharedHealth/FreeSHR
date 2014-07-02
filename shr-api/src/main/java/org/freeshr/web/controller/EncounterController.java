package org.freeshr.web.controller;

import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.service.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/encounter")
public class EncounterController {

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
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
}
