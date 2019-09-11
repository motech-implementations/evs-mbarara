package org.motechproject.evsmbarara.scheduler;

import org.joda.time.Period;
import org.motechproject.event.MotechEvent;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.scheduler.contract.RepeatingPeriodSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EvsMbararaScheduler {
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    public EvsMbararaScheduler(MotechSchedulerService motechSchedulerService) {
        this.motechSchedulerService = motechSchedulerService;
    }

    public void scheduleZetesUpdateJob(Date startDate, String zetesUrl, String zetesUsername, String zetesPassword) {
        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(EvsMbararaConstants.ZETES_URL, zetesUrl);
        eventParameters.put(EvsMbararaConstants.ZETES_USERNAME, zetesUsername);
        eventParameters.put(EvsMbararaConstants.ZETES_PASSWORD, zetesPassword);

        MotechEvent event = new MotechEvent(EvsMbararaConstants.ZETES_UPDATE_EVENT, eventParameters);

        Period period = Period.days(1);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate, null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }

    public void unscheduleZetesUpdateJob() {
        motechSchedulerService.safeUnscheduleAllJobs(EvsMbararaConstants.ZETES_UPDATE_EVENT);
    }
}
