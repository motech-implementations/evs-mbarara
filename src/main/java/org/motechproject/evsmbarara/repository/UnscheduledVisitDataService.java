package org.motechproject.evsmbarara.repository;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;

import java.util.List;


public interface UnscheduledVisitDataService extends MotechDataService<UnscheduledVisit> {

    @Lookup
    List<UnscheduledVisit> findByClinicLocation(@LookupField(name = "clinic.location",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<UnscheduledVisit> findByClinicLocationAndDate(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "clinic.location",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<UnscheduledVisit> findByClinicIdAndDateAndVisitId(
            @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id);

    long countFindByClinicIdAndDateAndVisitId(
            @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id);

    @Lookup
    List<UnscheduledVisit> findByDate(@LookupField(name = "date") Range<LocalDate> dateRange);

    @Lookup
    List<UnscheduledVisit> findByParticipantId(@LookupField(name = "subject.subjectId",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup
    List<UnscheduledVisit> findByParticipantIdAndDate(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "subject.subjectId", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup
    List<UnscheduledVisit> findByClinicIdAndDateRange(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "date") Range<LocalDate> date);

    long countFindByClinicIdAndDateRange(@LookupField(name = "clinic.id") Long clinicId,
                                         @LookupField(name = "date") Range<LocalDate> date);

    @Lookup
    List<UnscheduledVisit> findByClinicIdAndDate(@LookupField(name = "clinic.id") Long clinicId,
                                                 @LookupField(name = "date") LocalDate date);

    long countFindByClinicIdAndDate(@LookupField(name = "clinic.id") Long clinicId,
                                    @LookupField(name = "date") LocalDate date);
}
