package org.motechproject.evsmbarara.scheduler;

import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.scheduler.contract.RepeatingPeriodSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EvsMbararaScheduler {
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    public EvsMbararaScheduler(MotechSchedulerService motechSchedulerService) {
        this.motechSchedulerService = motechSchedulerService;
    }

    public void scheduleDailyReportJob(DateTime startDate) {
        Period period = Period.days(1);

        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(EvsMbararaConstants.DAILY_REPORT_EVENT_START_DATE, startDate);

        MotechEvent event = new MotechEvent(EvsMbararaConstants.DAILY_REPORT_EVENT, eventParameters);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate.toDate(), null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }

    public void unscheduleDailyReportJob() {
        motechSchedulerService.safeUnscheduleAllJobs(EvsMbararaConstants.DAILY_REPORT_EVENT);
    }

    public void scheduleClearExportTasksJob() {
        DateTime startDate =  DateUtil.newDateTime(LocalDate.now().plusDays(1),
            Time.parseTime(EvsMbararaConstants.CLEAR_EXPORT_TASKS_EVENT_START_TIME, ":"));
        Period period = Period.days(1);

        MotechEvent event = new MotechEvent(EvsMbararaConstants.CLEAR_EXPORT_TASKS_EVENT);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate.toDate(), null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }
}
