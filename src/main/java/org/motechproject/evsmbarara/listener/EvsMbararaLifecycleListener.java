package org.motechproject.evsmbarara.listener;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.evsmbarara.domain.Clinic;

public interface EvsMbararaLifecycleListener {

    @InstanceLifecycleListener(value = InstanceLifecycleListenerType.POST_CREATE)
    void addClinicToVisitBookingDetails(Clinic clinic);
}
