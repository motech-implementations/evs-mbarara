package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.dto.CapacityReportDto;
import org.motechproject.evsmbarara.web.domain.GridSettings;

import java.util.List;

public interface ReportService {

    List<CapacityReportDto> generateCapacityReports(GridSettings settings);
}
