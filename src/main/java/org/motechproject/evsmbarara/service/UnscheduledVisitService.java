package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.dto.UnscheduledVisitDto;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;

import java.io.IOException;

public interface UnscheduledVisitService {

    Records<UnscheduledVisitDto> getUnscheduledVisitsRecords(GridSettings settings) throws IOException;

    UnscheduledVisitDto addOrUpdate(UnscheduledVisitDto unscheduledVisitDto,
                                    Boolean ignoreLimitation);
}
