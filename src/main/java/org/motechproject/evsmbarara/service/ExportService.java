package org.motechproject.evsmbarara.service;

import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.evsmbarara.template.PdfBasicTemplate;
import org.motechproject.evsmbarara.template.XlsBasicTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public interface ExportService {

    void exportEntityToPDF(OutputStream outputStream, Class<?> entityDtoType, Class<?> entityType,
                           Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams)
            throws IOException;

    void exportEntityToPDF(PdfBasicTemplate template, Class<?> entityDtoType, Class<?> entityType,
                           Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams)
            throws IOException;

    void exportEntityToCSV(Writer writer, Class<?> entityDtoType, Class<?> entityType, Map<String, String> headerMap,
                           String lookup, String lookupFields, QueryParams queryParams) throws IOException;

    void exportEntityToExcel(XlsBasicTemplate template, Class<?> entityDtoType, Class<?> entityType,
                             Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams)
            throws IOException;

    void emailExportEntityToCSV(Writer writer, String entityClassName, Map<String, String> headerMap, String lookup,
                                String lookupFields, QueryParams queryParams, boolean showNullsCells) throws IOException;

    void exportEntityToCSV(Writer writer, String entityClassName, Map<String, String> headerMap, String lookup,
                           String lookupFields, QueryParams queryParams) throws IOException;

    <T> void exportEntity(List<T> entities, Map<String, String> headerMap, TableWriter tableWriter) throws IOException;

}
