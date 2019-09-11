package org.motechproject.evsmbarara.scheduler;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.service.ZetesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EvsMbararaEventListener {

    @Autowired
    private ZetesService zetesService;

    @MotechListener(subjects = { EvsMbararaConstants.ZETES_UPDATE_EVENT })
    public void zetesUpdate(MotechEvent event) {
        Object zetesUrl = event.getParameters().get(EvsMbararaConstants.ZETES_URL);
        Object username = event.getParameters().get(EvsMbararaConstants.ZETES_USERNAME);
        Object password = event.getParameters().get(EvsMbararaConstants.ZETES_PASSWORD);
        zetesService.sendUpdatedSubjects(zetesUrl.toString(), username.toString(), password.toString());
    }
}
