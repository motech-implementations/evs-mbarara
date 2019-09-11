package org.motechproject.evsmbarara.web;


import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.impl.csv.writer.CsvTableWriter;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.dto.CapacityReportDto;
import org.motechproject.evsmbarara.dto.PrimeVaccinationScheduleDto;
import org.motechproject.evsmbarara.dto.UnscheduledVisitDto;
import org.motechproject.evsmbarara.dto.VisitRescheduleDto;
import org.motechproject.evsmbarara.exception.EvsMbararaExportException;
import org.motechproject.evsmbarara.exception.EvsMbararaLookupException;
import org.motechproject.evsmbarara.helper.DtoLookupHelper;
import org.motechproject.evsmbarara.service.ExportService;
import org.motechproject.evsmbarara.service.ReportService;
import org.motechproject.evsmbarara.template.PdfBasicTemplate;
import org.motechproject.evsmbarara.template.PdfExportTemplate;
import org.motechproject.evsmbarara.template.XlsBasicTemplate;
import org.motechproject.evsmbarara.template.XlsExportTemplate;
import org.motechproject.evsmbarara.util.ExcelTableWriter;
import org.motechproject.evsmbarara.util.PdfTableWriter;
import org.motechproject.evsmbarara.util.QueryParamsBuilder;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.APPLICATION_PDF_CONTENT;
import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.TEXT_CSV_CONTENT;

@Controller
public class ExportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportController.class);

    private static final String PDF_EXPORT_FORMAT = "pdf";
    private static final String CSV_EXPORT_FORMAT = "csv";
    private static final String XLS_EXPORT_FORMAT = "xls";

    @Autowired
    private ExportService exportService;

    @Autowired
    private ReportService reportService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/exportInstances/screening", method = RequestMethod.GET)
    public void exportScreening(GridSettings settings, @RequestParam String exportRecords,
                                @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForScreeningAndUnscheduled(settings);
        exportEntity(newSettings, exportRecords, outputFormat, response, EvsMbararaConstants.SCREENING_NAME,
                null, Screening.class, EvsMbararaConstants.SCREENING_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/primeVaccinationSchedule", method = RequestMethod.GET)
    public void exportPrimeVaccinationSchedule(GridSettings settings, @RequestParam String exportRecords,
                                               @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForPrimeVaccinationSchedule(settings);

        exportEntity(newSettings, exportRecords, outputFormat, response, EvsMbararaConstants.PRIME_VACCINATION_SCHEDULE_NAME,
                PrimeVaccinationScheduleDto.class, Visit.class, EvsMbararaConstants.PRIME_VACCINATION_SCHEDULE_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/visitReschedule", method = RequestMethod.GET)
    public void exportVisitReschedule(GridSettings settings, @RequestParam String exportRecords,
                                      @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        exportEntity(newSettings, exportRecords, outputFormat, response, EvsMbararaConstants.VISIT_RESCHEDULE_NAME,
                VisitRescheduleDto.class, Visit.class, EvsMbararaConstants.VISIT_RESCHEDULE_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/unscheduledVisits", method = RequestMethod.GET)
    public void exportUnscheduledVisits(GridSettings settings, @RequestParam String exportRecords,
                                        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForScreeningAndUnscheduled(settings);

        exportEntity(newSettings, exportRecords, outputFormat, response, EvsMbararaConstants.UNSCHEDULED_VISITS_NAME,
                UnscheduledVisitDto.class, UnscheduledVisit.class, EvsMbararaConstants.UNSCHEDULED_VISIT_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportInstances/capacityReports", method = RequestMethod.GET)
    public void exportCapacityReports(GridSettings settings, @RequestParam String exportRecords,
                                      @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        List<CapacityReportDto> capacityReportDtoList = reportService.generateCapacityReports(settings);

        Integer recordsCount = StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords);

        if (recordsCount != null && capacityReportDtoList.size() > recordsCount) {
            capacityReportDtoList = capacityReportDtoList.subList(0, recordsCount);
        }

        exportEntity(outputFormat, response, EvsMbararaConstants.CAPACITY_REPORT_NAME, capacityReportDtoList, EvsMbararaConstants.CAPACITY_REPORT_FIELDS_MAP);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }

    private void exportEntity(GridSettings settings, String exportRecords, String outputFormat, HttpServletResponse response, //NO CHECKSTYLE ParameterNumber
                              String fileNameBeginning, Class<?> entityDtoType, Class<?> entityType, Map<String, String> headerMap) throws IOException {

        setResponseData(response, outputFormat, fileNameBeginning);

        QueryParams queryParams = new QueryParams(1, StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords),
                QueryParamsBuilder.buildOrderList(settings, getFields(settings)));

        try {
            if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
                PdfBasicTemplate template = new PdfExportTemplate(response.getOutputStream());

                exportService.exportEntityToPDF(template, entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
                exportService.exportEntityToCSV(response.getWriter(), entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
                XlsBasicTemplate template = new XlsExportTemplate(response.getOutputStream());

                exportService.exportEntityToExcel(template, entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            }
        } catch (IOException | EvsMbararaLookupException | EvsMbararaExportException e) {
            LOGGER.debug(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> getFields(GridSettings gridSettings) throws IOException {
        if (gridSettings.getFields() == null) {
            return null;
        } else {
            return objectMapper.readValue(gridSettings.getFields(), new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }

    private void exportEntity(String outputFormat, HttpServletResponse response, String fileNameBeginning, List<?> entities,
                              Map<String, String> headerMap) throws IOException {

        setResponseData(response, outputFormat, fileNameBeginning);

        try {
            if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
                PdfTableWriter tableWriter = new PdfTableWriter(new PdfExportTemplate(response.getOutputStream()));

                exportService.exportEntity(entities, headerMap, tableWriter);
            } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
                CsvTableWriter tableWriter = new CsvTableWriter(response.getWriter());

                exportService.exportEntity(entities, headerMap, tableWriter);
            } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
                ExcelTableWriter tableWriter = new ExcelTableWriter(new XlsExportTemplate(response.getOutputStream()));

                exportService.exportEntity(entities, headerMap, tableWriter);
            }
        } catch (IOException | EvsMbararaLookupException | EvsMbararaExportException e) {
            LOGGER.debug(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
