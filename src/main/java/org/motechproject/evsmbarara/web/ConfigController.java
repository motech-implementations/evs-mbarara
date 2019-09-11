package org.motechproject.evsmbarara.web;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.evsmbarara.domain.Config;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.scheduler.EvsMbararaScheduler;
import org.motechproject.evsmbarara.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
public class ConfigController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    @Qualifier("configService")
    private ConfigService configService;

    @Autowired
    private EvsMbararaScheduler evsMbararaScheduler;

    @RequestMapping(value = "/evs-mbarara-config", method = RequestMethod.GET)
    @ResponseBody
    public Config getConfig() {
        return configService.getConfig();
    }

    @RequestMapping(value = "/evs-mbarara-config", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Config updateConfig(@RequestBody Config config) {
        configService.updateConfig(config);

        evsMbararaScheduler.unscheduleZetesUpdateJob();
        scheduleJobs();

        return configService.getConfig();
    }

    @PreAuthorize("hasRole('manageEvsMbarara')")
    @RequestMapping(value = "/availableVisits", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getAvailableVisits() {
        return VisitType.getDisplayValues();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) throws IOException {
        LOGGER.error("Error while updating configs", e);
        return e.getMessage();
    }

    private void scheduleJobs() {
        if (configService.getConfig().getEnableZetesJob()) {
            String zetesUrl = configService.getConfig().getZetesUrl();
            String zetesUsername = configService.getConfig().getZetesUsername();
            String zetesPassword = configService.getConfig().getZetesPassword();

            LocalTime startTime = LocalTime.parse(
                    configService.getConfig().getStartTime(),
                    DateTimeFormat.forPattern(Config.TIME_PICKER_FORMAT)
            );
            Date startDate = startTime.toDateTimeToday().toDate();
            evsMbararaScheduler
                .scheduleZetesUpdateJob(startDate, zetesUrl, zetesUsername, zetesPassword);
        }
    }
}
