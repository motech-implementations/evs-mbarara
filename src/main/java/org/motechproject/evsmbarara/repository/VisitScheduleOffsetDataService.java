package org.motechproject.evsmbarara.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.evsmbarara.domain.VisitScheduleOffset;
import org.motechproject.evsmbarara.domain.enums.VisitType;

public interface VisitScheduleOffsetDataService extends MotechDataService<VisitScheduleOffset> {

    @Lookup
    VisitScheduleOffset findByVisitType(
            @LookupField(name = "visitType") VisitType visitType);
}
