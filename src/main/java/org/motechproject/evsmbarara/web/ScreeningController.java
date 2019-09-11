package org.motechproject.evsmbarara.web;


import org.motechproject.mds.dto.LookupDto;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.dto.ScreeningDto;
import org.motechproject.evsmbarara.exception.EvsMbararaLookupException;
import org.motechproject.evsmbarara.exception.LimitationExceededException;
import org.motechproject.evsmbarara.helper.DtoLookupHelper;
import org.motechproject.evsmbarara.service.LookupService;
import org.motechproject.evsmbarara.service.ScreeningService;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/screenings")
@PreAuthorize(EvsMbararaConstants.HAS_SCREENING_TAB_ROLE)
public class ScreeningController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreeningController.class);

    @Autowired
    private ScreeningService screeningService;

    @Autowired
    private LookupService lookupService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Records<Screening> getScreenings(GridSettings settings) throws IOException {
        return screeningService.getScreenings(DtoLookupHelper.changeLookupForScreeningAndUnscheduled(settings));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ScreeningDto getScreeningById(@PathVariable Long id) {
        return screeningService.getScreeningById(id);
    }

    @RequestMapping(value = "/new/{ignoreLimitation}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public Object addOrUpdateScreening(@PathVariable Boolean ignoreLimitation, @RequestBody ScreeningDto screening) {
        try {
            return screeningService.addOrUpdate(screening, ignoreLimitation);
        } catch (LimitationExceededException e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public void cancelScreening(@RequestBody Long id) {
        screeningService.cancelScreening(id);
    }

    @RequestMapping(value = "/activate/{ignoreLimitation}", method = RequestMethod.POST)
    @ResponseBody
    public String activateScreening(@PathVariable Boolean ignoreLimitation, @RequestBody Long id) {
        try {
            screeningService.activateScreening(id, ignoreLimitation);
        } catch (LimitationExceededException e) {
            return e.getMessage();
        }
        return null;
    }

    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    @ResponseBody
    public void completeScreening(@RequestBody Long id) {
        screeningService.completeScreening(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleBadRequest(Exception e) {
        LOGGER.debug("Error while add or updating screening", e);
        return e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.debug(e.getMessage(), e);
        return e.getMessage();
    }

    @RequestMapping(value = "/getLookupsForScreening", method = RequestMethod.GET)
    @ResponseBody
    public List<LookupDto> getLookupsForScreening() {
        List<LookupDto> ret = new ArrayList<>();
        List<LookupDto> availableLookups;
        try {
            availableLookups = lookupService.getAvailableLookups(Screening.class.getName());
        } catch (EvsMbararaLookupException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        List<String> lookupList = EvsMbararaConstants.AVAILABLE_LOOKUPS_FOR_SCREENINGS;
        for (LookupDto lookupDto : availableLookups) {
            if (lookupList.contains(lookupDto.getLookupName())) {
                ret.add(lookupDto);
            }
        }
        return ret;
    }
}
