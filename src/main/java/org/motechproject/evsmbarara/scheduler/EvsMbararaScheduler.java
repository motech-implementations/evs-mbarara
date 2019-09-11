package org.motechproject.evsmbarara.scheduler;

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

}
