package org.freeshr.web.controller;

import org.freeshr.shr.encounter.model.Encounter;
import org.freeshr.shr.encounter.service.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/encounter")
public class EncounterController {

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void create(@RequestBody Encounter encounter) {
        encounterService.ensureCreated(encounter);
    }
}
