package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.dto.VisitRescheduleDto;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;

import java.io.IOException;

public interface VisitRescheduleService {

    Records<VisitRescheduleDto> getVisitsRecords(GridSettings settings) throws IOException;

    VisitRescheduleDto saveVisitReschedule(VisitRescheduleDto visitRescheduleDto,
                                           Boolean ignoreLimitation);
}
