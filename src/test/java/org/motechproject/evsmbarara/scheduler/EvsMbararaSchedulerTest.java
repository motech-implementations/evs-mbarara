package org.motechproject.evsmbarara.scheduler;


import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.event.MotechEvent;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.scheduler.contract.RepeatingPeriodSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class EvsMbararaSchedulerTest {

    @Mock
    private MotechSchedulerService motechSchedulerService;

    private EvsMbararaScheduler evsMbararaScheduler;

    @Before
    public void setUp() {
        initMocks(this);
        evsMbararaScheduler = new EvsMbararaScheduler(motechSchedulerService);
    }

    @After
    public void cleanup() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldScheduleZetesUpdateJob() {
        Date startDate = LocalDate.now().toDate();
        String zetesUrl = "zetesUrl";
        String zetesUsername = "username";
        String zetesPassword = "password";

        evsMbararaScheduler.scheduleZetesUpdateJob(startDate, zetesUrl, zetesUsername, zetesPassword);

        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(EvsMbararaConstants.ZETES_URL, zetesUrl);
        eventParameters.put(EvsMbararaConstants.ZETES_USERNAME, zetesUsername);
        eventParameters.put(EvsMbararaConstants.ZETES_PASSWORD, zetesPassword);
        MotechEvent event = new MotechEvent(EvsMbararaConstants.ZETES_UPDATE_EVENT, eventParameters);
        Period period = Period.days(1);
        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate, null, period, true);

        verify(motechSchedulerService).safeScheduleRepeatingPeriodJob(job);
    }

    @Test
    public void shouldUnscheduleZetesUpdateJob() {
        evsMbararaScheduler.unscheduleZetesUpdateJob();
        verify(motechSchedulerService).safeUnscheduleAllJobs(EvsMbararaConstants.ZETES_UPDATE_EVENT);
    }
}
