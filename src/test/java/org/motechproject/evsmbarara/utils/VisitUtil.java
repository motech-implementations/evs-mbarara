package org.motechproject.evsmbarara.utils;


import org.joda.time.LocalDate;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.VisitType;

public final class VisitUtil {

    private VisitUtil() {
    }

    public static Visit createVisit(Subject subject, VisitType type, LocalDate date,
                                    LocalDate dateProjected, String owner) {
        Visit visit = new Visit();
        visit.setSubject(subject);
        visit.setType(type);
        visit.setDate(date);
        visit.setDateProjected(dateProjected);
        visit.setOwner(owner);

        return visit;
    }
}
