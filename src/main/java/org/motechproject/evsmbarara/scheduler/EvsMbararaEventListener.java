package org.motechproject.evsmbarara.scheduler;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.springframework.stereotype.Component;

@Component
public class EvsMbararaEventListener {

    @MotechListener(subjects = { "event" })
    public void event(MotechEvent event) {
    }
}
