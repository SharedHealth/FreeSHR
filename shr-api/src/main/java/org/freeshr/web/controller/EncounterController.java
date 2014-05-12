package org.freeshr.web.controller;

import org.freeshr.encounter.model.Encounter;
import org.freeshr.encounter.service.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/encounter")
public class EncounterController {

    private EncounterService encounterService;

    @Autowired
    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(Encounter encounter) {
        encounterService.ensureCreated(encounter);
        return "Hello world";
    }
}
