package org.motechproject.evsmbarara.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EvsMbararaConstants {

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    public static final String CSV_EXPORT_FORMAT = "csv";

    public static final String TEXT_CSV_CONTENT = "text/csv";
    public static final String APPLICATION_PDF_CONTENT = "application/pdf";

    public static final Map<String, Float> REPORT_COLUMN_WIDTHS = new LinkedHashMap<String, Float>() {
        {
            put("Participant Id", 64f); //NO CHECKSTYLE MagicNumber
            put("Age", 24f);
            put("SMS", 32f);
        }
    };

    public static final String API_KEY = "api_key";
    public static final String MESSAGE_ID = "message_id";
    public static final String STATUS_CALLBACK_URL = "status_callback_url";
    public static final String SUBSCRIBERS = "subscribers";
    public static final String PHONE = "phone";
    public static final String LANGUAGE = "language";
    public static final String SEND_SMS_IF_VOICE_FAILS = "send_sms_if_voice_fails";
    public static final String DETECT_VOICEMAIL = "detect_voicemail_action";
    public static final String RETRY_ATTEMPTS_SHORT = "retry_attempts_short";
    public static final String RETRY_DELAY_SHORT = "retry_delay_short";
    public static final String RETRY_ATTEMPTS_LONG = "retry_attempts_long";
    public static final String RETRY_ATTEMPTS_LONG_DEFAULT = "1";
    public static final String SUBJECT_ID = "subject_id";
    public static final String SUBJECT_PHONE_NUMBER = "subscriber_phone";

    public static final String VISIT_RESCHEDULE_TAB_PERMISSION = "evsMbararaVisitRescheduleBookingTab";
    public static final String UNSCHEDULED_VISITS_TAB_PERMISSION = "unscheduledVisitsTab";
    public static final String REPORTS_TAB_PERMISSION = "evsMbararaReportsTab";
    public static final String SUBJECTS_TAB_PERMISSION = "evsMbararaSubjectsTab";

    public static final String HAS_VISIT_RESCHEDULE_TAB_ROLE = "hasRole('" + VISIT_RESCHEDULE_TAB_PERMISSION + "')";
    public static final String HAS_UNSCHEDULED_VISITS_TAB_ROLE = "hasRole('" + UNSCHEDULED_VISITS_TAB_PERMISSION + "')";
    public static final String HAS_REPORTS_TAB_ROLE = "hasRole('" + REPORTS_TAB_PERMISSION + "')";
    public static final String HAS_SUBJECTS_TAB_ROLE = "hasRole('" + SUBJECTS_TAB_PERMISSION + "')";

    public static final String UNSCHEDULED_VISITS_NAME = "Unscheduled_Visits";
    public static final String VISIT_RESCHEDULE_NAME = "VisitReschedule";
    public static final String CAPACITY_REPORT_NAME = "CapacityReport";

    public static final Map<String, String> VISIT_RESCHEDULE_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id", "participantId");
            put("Visit Type", "visitType");
            put("Actual Date", "actualDate");
            put("Planned Date", "plannedDate");
        }
    };

    public static final Map<String, String> UNSCHEDULED_VISIT_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id", "participantId");
            put("Date", "date");
            put("Purpose of the visit", "purpose");
        }
    };

    public static final Map<String, String> CAPACITY_REPORT_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Date", "date");
            put("Max. Capacity", "maxCapacity");
            put("Available Capacity", "availableCapacity");
            put("Screening Slot Remaining", "screeningSlotRemaining");
            put("Vaccine Slot Remaining", "vaccineSlotRemaining");
        }
    };

    public static final List<String> AVAILABLE_LOOKUPS_FOR_VISIT_RESCHEDULE = new ArrayList<>(Arrays.asList(
            "Find By Visit Planned Date",
            "Find By Visit Type And Planned Date",
            "Find By Visit Actual Date",
            "Find By Visit Type And Actual Date",
            "Find By Participant Id"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_UNSCHEDULED = new ArrayList<>(
        Collections.singletonList("Find By Participant Id"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_CAPACITY_REPORT = new ArrayList<>(Collections.singletonList(
            "Find By Location"));

    private EvsMbararaConstants() {
    }
}
