package org.motechproject.evsmbarara.web;


import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(value = "participants")
public class RepositoryController {

    @Autowired
    private SubjectDataService subjectDataService;

    @RequestMapping(value = "/screened", method = RequestMethod.GET)
    @ResponseBody
    public List<Subject> getScreenedParticipants() throws IOException {
        return subjectDataService.findByVisitTypeAndActualDate(VisitType.SCREENING, null);
    }

}
