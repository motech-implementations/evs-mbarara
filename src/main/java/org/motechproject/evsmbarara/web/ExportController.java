package org.motechproject.evsmbarara.web;

import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.APPLICATION_PDF_CONTENT;
import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.CSV_EXPORT_FORMAT;
import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.PDF_EXPORT_FORMAT;
import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.TEXT_CSV_CONTENT;
import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.XLS_EXPORT_FORMAT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.IvrAndSmsStatisticReport;
import org.motechproject.evsmbarara.domain.SubjectEnrollments;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.dto.ExportResult;
import org.motechproject.evsmbarara.dto.ExportStatusResponse;
import org.motechproject.evsmbarara.dto.IvrAndSmsStatisticReportDto;
import org.motechproject.evsmbarara.dto.MissedVisitsReportDto;
import org.motechproject.evsmbarara.dto.OptsOutOfMotechMessagesReportDto;
import org.motechproject.evsmbarara.dto.UnscheduledVisitDto;
import org.motechproject.evsmbarara.dto.VisitRescheduleDto;
import org.motechproject.evsmbarara.helper.DtoLookupHelper;
import org.motechproject.evsmbarara.service.ExportService;
import org.motechproject.evsmbarara.util.QueryParamsBuilder;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.mds.query.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ExportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportController.class);

    @Autowired
    private ExportService exportService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/export/{exportId}/status", method = RequestMethod.GET)
    public ResponseEntity<ExportStatusResponse> exportStatus(@PathVariable UUID exportId) {
        ExportStatusResponse exportStatus = exportService.getExportStatus(exportId);

        return new ResponseEntity<>(exportStatus, HttpStatus.OK);
    }

    @RequestMapping(value = "/export/{exportId}/results", method = RequestMethod.GET)
    public void exportResults(@PathVariable UUID exportId, HttpServletResponse response) throws IOException {

        ExportResult exportResult = exportService.getExportResults(exportId);

        setResponseData(response, exportResult.getExportFormat(), exportResult.getFileName());

        OutputStream responseOutputStream = response.getOutputStream();

        exportResult.getOutputStream().writeTo(responseOutputStream);
        responseOutputStream.close();
    }

    @RequestMapping(value = "/export/{exportId}/cancel", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void exportCancel(@PathVariable UUID exportId) {
        exportService.cancelExport(exportId);
    }

    @RequestMapping(value = "/exportInstances/visitReschedule", method = RequestMethod.GET)
    public ResponseEntity<String> exportVisitReschedule(GridSettings settings, @RequestParam String exportRecords,
                                      @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, EvsMbararaConstants.VISIT_RESCHEDULE_NAME,
                VisitRescheduleDto.class, Visit.class, EvsMbararaConstants.VISIT_RESCHEDULE_FIELDS_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportInstances/unscheduledVisits", method = RequestMethod.GET)
    public ResponseEntity<String> exportUnscheduledVisits(GridSettings settings, @RequestParam String exportRecords,
                                        @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForUnscheduled(settings);

        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, EvsMbararaConstants.UNSCHEDULED_VISITS_NAME,
                UnscheduledVisitDto.class, UnscheduledVisit.class, EvsMbararaConstants.UNSCHEDULED_VISIT_FIELDS_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportDailyClinicVisitScheduleReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportDailyClinicVisitScheduleReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat) throws IOException {

        UUID exportId = exportEntity(settings, exportRecords, outputFormat, EvsMbararaConstants.DAILY_CLINIC_VISIT_SCHEDULE_REPORT_NAME,
            null, Visit.class, EvsMbararaConstants.DAILY_CLINIC_VISIT_SCHEDULE_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportFollowupsMissedClinicVisitsReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportFollowupsMissedClinicVisitsReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForFollowupsMissedClinicVisitsReport(settings);

        if (newSettings == null) {
            return new ResponseEntity<>("Invalid lookups params", HttpStatus.BAD_REQUEST);
        }
        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, EvsMbararaConstants.FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_NAME,
                MissedVisitsReportDto.class, Visit.class, EvsMbararaConstants.FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportMandEMissedClinicVisitsReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportMandEMissedClinicVisitsReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForMandEMissedClinicVisitsReport(settings);

        if (newSettings == null) {
            return new ResponseEntity<>("Invalid lookups params", HttpStatus.BAD_REQUEST);
        }
        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, EvsMbararaConstants.M_AND_E_MISSED_CLINIC_VISITS_REPORT_NAME,
                MissedVisitsReportDto.class, Visit.class, EvsMbararaConstants.M_AND_E_MISSED_CLINIC_VISITS_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportOptsOutOfMotechMessagesReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportOptsOutOfMotechMessagesReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForOptsOutOfMotechMessagesReport(settings);

        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, EvsMbararaConstants.OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_NAME,
                OptsOutOfMotechMessagesReportDto.class, SubjectEnrollments.class, EvsMbararaConstants.OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportIvrAndSmsStatisticReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportIvrAndSmsStatisticReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat) throws IOException {

        UUID exportId = exportEntity(settings, exportRecords, outputFormat, EvsMbararaConstants.IVR_AND_SMS_STATISTIC_REPORT_NAME,
            IvrAndSmsStatisticReportDto.class, IvrAndSmsStatisticReport.class, EvsMbararaConstants.IVR_AND_SMS_STATISTIC_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportSubjectEnrollment", method = RequestMethod.GET)
    public ResponseEntity<String> exportSubjectEnrollment(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat) throws IOException {

        UUID exportId = exportEntity(settings, exportRecords, outputFormat, EvsMbararaConstants.SUBJECT_ENROLLMENTS_NAME,
            null, SubjectEnrollments.class, EvsMbararaConstants.SUBJECT_ENROLLMENTS_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }

    private UUID exportEntity(GridSettings settings, String exportRecords, String outputFormat,
        String fileNameBeginning, Class<?> entityDtoType, Class<?> entityType, Map<String, String> headerMap) throws IOException {

        QueryParams queryParams = new QueryParams(1,
            StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords),
            QueryParamsBuilder.buildOrderList(settings, getFields(settings)));

        return exportService.exportEntity(outputFormat, fileNameBeginning, entityDtoType,
            entityType, headerMap, settings.getLookup(), settings.getFields(), queryParams);
    }

    private Map<String, Object> getFields(GridSettings gridSettings) throws IOException {
        if (gridSettings.getFields() == null) {
            return null;
        } else {
            return objectMapper.readValue(gridSettings.getFields(), new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }

    private void setResponseData(HttpServletResponse response, String outputFormat, String fileNameBeginning) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        final String fileName = fileNameBeginning + "_" + DateTime.now().toString(dateTimeFormatter);

        if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType(APPLICATION_PDF_CONTENT);
        } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType(TEXT_CSV_CONTENT);
        } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType("application/vnd.ms-excel");
        } else {
            throw new IllegalArgumentException("Invalid export format: " + outputFormat);
        }
        response.setCharacterEncoding(UTF_8);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + fileName + "." + outputFormat.toLowerCase());
    }
}
