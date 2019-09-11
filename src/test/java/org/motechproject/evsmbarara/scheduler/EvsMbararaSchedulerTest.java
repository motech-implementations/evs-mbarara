package org.motechproject.evsmbarara.scheduler;


import static org.mockito.MockitoAnnotations.initMocks;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.motechproject.scheduler.service.MotechSchedulerService;

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

}
