package org.motechproject.evsmbarara.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.DateFilter;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.dto.LookupFieldDto;
import org.motechproject.mds.dto.SettingDto;

public final class DtoLookupHelper {

    private static final String NOT_BLANK_REGEX = ".";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<VisitType> AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN =
        new HashSet<>(Arrays.asList(VisitType.BOOST_VACCINATION_DAY, VisitType.D1_VISIT,
            VisitType.D3_VISIT, VisitType.D28_VISIT, VisitType.D7_VISIT, VisitType.D57_VISIT,
            VisitType.D59_VISIT, VisitType.D63_VISIT, VisitType.D77_VISIT, VisitType.D84_VISIT,
            VisitType.D180_VISIT, VisitType.D365_VISIT, VisitType.D720_VISIT));

    private DtoLookupHelper() {
    }

    public static GridSettings changeLookupForUnscheduled(GridSettings settings) throws IOException {
        Map<String, Object> fieldsMap = new HashMap<>();
        DateFilter dateFilter = settings.getDateFilter();

        if (dateFilter != null) {

            if (StringUtils.isBlank(settings.getFields())) {
                settings.setFields("{}");
            }

            String lookup = settings.getLookup();
            if (StringUtils.isBlank(lookup)) {
                settings.setLookup("Find By Date");
            } else {
                fieldsMap = getFields(settings.getFields());
                settings.setLookup(lookup + " And Date");
            }

            Map<String, String> rangeMap = getDateRangeFromFilter(settings);

            fieldsMap.put(UnscheduledVisit.DATE_PROPERTY_NAME, rangeMap);
            settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        }
        return settings;
    }

    //CHECKSTYLE:OFF: checkstyle:cyclomaticcomplexity
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public static GridSettings changeLookupForVisitReschedule(GridSettings settings) throws IOException {  //NO CHECKSTYLE CyclomaticComplexity
        Map<String, Object> fieldsMap = new HashMap<>();

        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }

        if (StringUtils.isBlank(settings.getLookup())) {
            settings.setLookup("Find By Visit Type Set And Planned Date");
            fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
        } else {
            fieldsMap = getFields(settings.getFields());
        }

        Map<String, String> rangeMap = getDateRangeFromFilter(settings);
        String lookup = settings.getLookup();

        switch (lookup) {
            case "Find By Visit Type And Planned Date":
                break;
            case "Find By Visit Planned Date":
                fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
                settings.setLookup(lookup + " Range And Visit Type Set");
                break;
            case "Find By Visit Type Set And Planned Date":
                if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
                    settings.setLookup(lookup + " Range");
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, rangeMap);
                }
                break;
            case "Find By Visit Type And Actual Date":
                if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
                    settings.setLookup(lookup + " Range And Planned Date Range");
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, rangeMap);
                }
                break;
            default:
                if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
                    settings.setLookup(lookup + " And Visit Type Set And Planned Date Range");
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, rangeMap);
                    fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
                } else {
                    fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
                    settings.setLookup(lookup + " And Visit Type Set");
                }
                break;
        }
        settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        return settings;
    }
    //CHECKSTYLE:ON: checkstyle:cyclomaticcomplexity

    public static LookupDto changeVisitTypeLookupOptionsOrder(LookupDto lookupDto) {
        if (lookupDto.getLookupFields() != null) {
            for (LookupFieldDto lookupFieldDto: lookupDto.getLookupFields()) {
                if (Visit.VISIT_TYPE_DISPLAY_NAME.equals(lookupFieldDto.getDisplayName())
                        || Visit.VISIT_TYPE_DISPLAY_NAME.equals(lookupFieldDto.getRelatedFieldDisplayName())) {
                    for (SettingDto settingDto : lookupFieldDto.getSettings()) {
                        if ("mds.form.label.values".equals(settingDto.getName())) {
                            List<String> visitTypes = new ArrayList<>();
                            for (VisitType visitType: VisitType.values()) {
                                visitTypes.add(visitType.toString() + ": " + visitType.getDisplayValue());
                            }
                            settingDto.setValue(visitTypes);
                        }
                    }
                }
            }
        }

        return lookupDto;
    }

    private static Map<String, String> getDateRangeFromFilter(GridSettings settings) {
        DateFilter dateFilter = settings.getDateFilter();

        if (dateFilter == null) {
            return null;
        }

        Map<String, String> rangeMap = new HashMap<>();

        if (DateFilter.DATE_RANGE.equals(dateFilter)) {
            rangeMap.put("min", settings.getStartDate());
            rangeMap.put("max", settings.getEndDate());
        } else {
            Range<LocalDate> dateRange = dateFilter.getRange();
            rangeMap.put("min", dateRange.getMin().toString(EvsMbararaConstants.SIMPLE_DATE_FORMAT));
            rangeMap.put("max", dateRange.getMax().toString(EvsMbararaConstants.SIMPLE_DATE_FORMAT));
        }

        return rangeMap;
    }

    private static Map<String, Object> getFields(String lookupFields) throws IOException {
        return OBJECT_MAPPER.readValue(lookupFields, new TypeReference<HashMap>() {
        }); //NO CHECKSTYLE WhitespaceAround
    }

    private static Map<String, String> getFieldsMap(String lookupFields) throws IOException {
        return OBJECT_MAPPER.readValue(lookupFields, new TypeReference<HashMap>() {
        }); //NO CHECKSTYLE WhitespaceAround
    }
}
