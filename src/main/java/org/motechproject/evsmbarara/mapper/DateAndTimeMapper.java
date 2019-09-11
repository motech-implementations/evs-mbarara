package org.motechproject.evsmbarara.mapper;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.commons.date.model.Time;

import static org.motechproject.evsmbarara.constants.EvsMbararaConstants.SIMPLE_DATE_FORMAT;

public class DateAndTimeMapper {

    String dateToString(LocalDate date) {
        return date.toString(SIMPLE_DATE_FORMAT);
    }

    LocalDate toDate(String date) {
        return LocalDate.parse(date, DateTimeFormat.forPattern(SIMPLE_DATE_FORMAT));
    }

    String timeToString(Time time) {
        return time.toString();
    }

    Time toTime(String time) {
        return Time.valueOf(time);
    }
}
