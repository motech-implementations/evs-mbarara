package org.motechproject.evsmbarara.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;
import org.motechproject.evsmbarara.dto.UnscheduledVisitDto;
import org.motechproject.evsmbarara.exception.EvsMbararaLookupException;
import org.motechproject.evsmbarara.helper.DtoLookupHelper;
import org.motechproject.evsmbarara.service.LookupService;
import org.motechproject.evsmbarara.service.UnscheduledVisitService;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;
import org.motechproject.mds.dto.LookupDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("unscheduledVisits")
@PreAuthorize(EvsMbararaConstants.HAS_UNSCHEDULED_VISITS_TAB_ROLE)
public class UnscheduledVisitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnscheduledVisitController.class);

    @Autowired
    private LookupService lookupService;

    @Autowired
    private UnscheduledVisitService unscheduledVisitService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Records<UnscheduledVisitDto> getUnscheduledVisits(GridSettings settings) throws IOException {
        return unscheduledVisitService.getUnscheduledVisitsRecords(DtoLookupHelper.changeLookupForUnscheduled(settings));
    }

    @RequestMapping(value = "/new/{ignoreLimitation}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public Object addOrUpdateUnscheduled(@PathVariable Boolean ignoreLimitation,
                                         @RequestBody UnscheduledVisitDto unscheduledVisitDto) {
        return unscheduledVisitService.addOrUpdate(unscheduledVisitDto, ignoreLimitation);
    }

    @RequestMapping(value = "/getLookupsForUnscheduled", method = RequestMethod.GET)
    @ResponseBody
    public List<LookupDto> getLookupsForUnscheduled() {
        List<LookupDto> ret = new ArrayList<>();
        List<LookupDto> availableLookups;
        try {
            availableLookups = lookupService.getAvailableLookups(UnscheduledVisit.class.getName());
        } catch (EvsMbararaLookupException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        List<String> lookupList = EvsMbararaConstants.AVAILABLE_LOOKUPS_FOR_UNSCHEDULED;
        for (LookupDto lookupDto : availableLookups) {
            if (lookupList.contains(lookupDto.getLookupName())) {
                ret.add(lookupDto);
            }
        }
        return ret;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }
}
