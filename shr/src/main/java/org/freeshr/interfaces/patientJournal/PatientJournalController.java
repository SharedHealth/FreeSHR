package org.freeshr.interfaces.patientJournal;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PatientJournalController {
    @RequestMapping(value="/journal/{healthId}", method = RequestMethod.GET)
    public @ResponseBody String journal(@PathVariable("healthId")
                          String healthId) {
        return "hello world " + healthId;
    }
}
