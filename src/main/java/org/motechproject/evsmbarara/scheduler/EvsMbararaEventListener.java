package org.motechproject.evsmbarara.scheduler;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.evsmbarara.exception.EvsInitiateCallException;
import org.motechproject.evsmbarara.helper.IvrCallHelper;
import org.motechproject.evsmbarara.service.EvsEnrollmentService;
import org.motechproject.messagecampaign.EventKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EvsMbararaEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvsMbararaEventListener.class);

    @Autowired
    private EvsEnrollmentService evsEnrollmentService;

    @Autowired
    private IvrCallHelper ivrCallHelper;

    @MotechListener(subjects = EventKeys.CAMPAIGN_COMPLETED)
    public void completeCampaign(MotechEvent event) {
        String campaignName = (String) event.getParameters().get(EventKeys.CAMPAIGN_NAME_KEY);
        String externalId = (String) event.getParameters().get(EventKeys.EXTERNAL_ID_KEY);

        evsEnrollmentService.completeCampaign(externalId, campaignName);
    }

    @MotechListener(subjects = EventKeys.SEND_MESSAGE)
    public void initiateIvrCall(MotechEvent event) {
        LOGGER.debug("Handling Motech event {}: {}", event.getSubject(), event.getParameters().toString());

        String messageKey = (String) event.getParameters().get(EventKeys.MESSAGE_KEY);
        String externalId = (String) event.getParameters().get(EventKeys.EXTERNAL_ID_KEY);

        try {
            ivrCallHelper.initiateIvrCall(messageKey, externalId);
        } catch (EvsInitiateCallException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
