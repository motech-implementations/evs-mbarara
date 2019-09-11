package org.motechproject.evsmbarara.repository;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.VisitType;

import java.util.List;
import java.util.Set;

public interface VisitDataService extends MotechDataService<Visit> {

    @Lookup(name = "Find By exact Participant Id")
    List<Visit> findBySubjectId(
            @LookupField(name = "subject.subjectId") String subjectId);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateLessOrEqual(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date", customOperator = Constants.Operators.LT_EQ) LocalDate date);

    /**
     * UI Lookups
     */

    @Lookup
    List<Visit> findByParticipantId(@LookupField(name = "subject.subjectId",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup
    List<Visit> findByVisitType(@LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByVisitActualDate(
            @LookupField(name = "date") Range<LocalDate> date);

    @Lookup
    List<Visit> findByVisitActualDateRange(
            @LookupField(name = "date") Range<LocalDate> date);

    @Lookup
    List<Visit> findByVisitPlannedDateRange(
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitPlannedDate(
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateRange(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date") Range<LocalDate> date);

    @Lookup
    List<Visit> findByVisitTypeAndActualDate(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date") Range<LocalDate> date);

    /**
     * Reschedule Screen Lookups
     */

    @Lookup
    List<Visit> findByVisitTypeSetAndPlannedDate(
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByParticipantIdAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndPlannedDate(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndPlannedDateRange(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitActualDateAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitPlannedDateRangeAndVisitTypeSet(
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate,
            @LookupField(name = "type") Set<VisitType> typeSet);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateRangeAndPlannedDateRange(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitActualDateAndVisitTypeSet(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "type") Set<VisitType> typeSet);

    @Lookup
    List<Visit> findByParticipantIdAndVisitTypeSet(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") Set<VisitType> typeSet);
}
