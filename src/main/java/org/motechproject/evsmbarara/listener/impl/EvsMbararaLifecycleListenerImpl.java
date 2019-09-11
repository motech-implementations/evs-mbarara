package org.motechproject.evsmbarara.listener.impl;

import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.listener.EvsMbararaLifecycleListener;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("evsMbararaLifecycleListener")
public class EvsMbararaLifecycleListenerImpl implements EvsMbararaLifecycleListener {

    @Autowired
    private VisitDataService visitDataService;

    @Override
    public void addClinicToVisitBookingDetails(Clinic clinic) {
        List<Visit> visits = visitDataService.findByExactParticipantSiteId(clinic.getSiteId());

        for (Visit details : visits) {
            details.setClinic(clinic);
            visitDataService.update(details);
        }
    }
}
