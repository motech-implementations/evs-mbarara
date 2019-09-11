package org.motechproject.evsmbarara.service;

import org.joda.time.LocalDate;


import java.util.Map;

public interface VisitScheduleService {

    Map<String, String> getPrimeVaccinationDateAndDateRange(String subjectId);

    Map<String, String> calculatePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate, boolean ignoreLimits);

    void savePlannedVisitDates(String subjectId, LocalDate primeVaccinationDate, boolean ignoreLimits);
}
