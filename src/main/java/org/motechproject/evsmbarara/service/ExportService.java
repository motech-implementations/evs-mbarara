package org.motechproject.evsmbarara.service;

import java.util.Map;
import java.util.UUID;
import org.motechproject.evsmbarara.dto.ExportResult;
import org.motechproject.evsmbarara.dto.ExportStatusResponse;
import org.motechproject.mds.query.QueryParams;

public interface ExportService {

    UUID exportEntity(String outputFormat, String fileName, Class<?> entityDtoType, Class<?> entityType,  //NO CHECKSTYLE ParameterNumber
        Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams);

    UUID exportEntity(String outputFormat, String fileName, String entityName,
        Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams);

    ExportStatusResponse getExportStatus(UUID exportId);

    ExportResult getExportResults(UUID exportId);

    void cancelExport(UUID exportId);

    void cancelAllExportTasks();
}
