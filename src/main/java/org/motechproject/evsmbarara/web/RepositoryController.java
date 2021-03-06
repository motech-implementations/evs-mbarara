package org.motechproject.evsmbarara.web;

import java.util.List;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "participants")
public class RepositoryController {

    @Autowired
    private SubjectDataService subjectDataService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public List<Subject> getAllParticipants() {
        return subjectDataService.retrieveAll();
    }
}
