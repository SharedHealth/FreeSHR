package org.freeshr.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/encounter")
public class EncounterController {


    @Autowired
    public EncounterController() {
    }

    @RequestMapping(method = RequestMethod.POST)
    public String create() {
        return "Hello world";
    }
}
