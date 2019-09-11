package org.motechproject.evsmbarara.web;

import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller("/clinics")
public class ClinicController {

    private static final List<String> CLINIC_FIELDS = new ArrayList<>(Arrays.asList("Site Id", "Location", "Max Capacity By Day",
            "Max Screening Visits", "Max Prime Visits", "Max Booster Visits", "Amount of Rooms", "Max Prime First Follow Up Visits",
            "Max Prime Second Follow Up Visits", "Max Prime Third Follow Up Visits", "Max Booster First Follow Up Visits",
            "Max Three Months Post Prime Visits", "Max Six Months Post Prime Visits", "Max Twelve Months Post Prime Visits",
            "Max Twenty Four Months Post Prime Visits", "Max Thirty Six Months Post Prime Visits",
            "Max Forty Eight Months Post Prime Visits", "Max Sixty Months Post Prime Visits"));

    @Autowired
    private ClinicService clinicService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    private List<Clinic> getSites() {
        return clinicService.getClinics();
    }

    @RequestMapping(value = "/clinicFields", method = RequestMethod.GET)
    @ResponseBody
    private List<String> getClinicFields() {
        return CLINIC_FIELDS;
    }
}
