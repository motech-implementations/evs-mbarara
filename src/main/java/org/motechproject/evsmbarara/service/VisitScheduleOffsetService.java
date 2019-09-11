package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.domain.VisitScheduleOffset;
import org.motechproject.evsmbarara.domain.enums.VisitType;

import java.util.List;
import java.util.Map;


public interface VisitScheduleOffsetService {

    VisitScheduleOffset findByVisitType(VisitType visitType);

    List<VisitScheduleOffset> getAll();

    Map<VisitType, VisitScheduleOffset> getAllAsMap();
}
