package org.motechproject.evsmbarara.repository;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.enums.VisitType;

import java.util.List;

/**
 * Interface for repository that persists simple records and allows CRUD.
 * MotechDataService base class will provide the implementation of this class as well
 * as methods for adding, deleting, saving and finding all instances.  In this class we
 * define and custom lookups we may need.
 */
public interface SubjectDataService extends MotechDataService<Subject> {

    @Lookup(name = "Find unique By Participant Id")
    Subject findBySubjectId(@LookupField(name = "subjectId") String subjectId);

    @Lookup(name = "Find By Primer Vaccination Date Range")
    List<Subject> findByPrimerVaccinationDateRange(@LookupField(name = "primerVaccinationDate")
                                                           Range<LocalDate> dateRange);

    @Lookup(name = "Find By Booster Vaccination Date Range")
    List<Subject> findByBoosterVaccinationDateRange(@LookupField(name = "boosterVaccinationDate")
                                                            Range<LocalDate> dateRange);

    @Lookup(name = "Find By Primer Vaccination Date")
    List<Subject> findByPrimerVaccinationDate(
            @LookupField(name = "primerVaccinationDate") LocalDate dateRange);

    @Lookup(name = "Find By Booster Vaccination Date")
    List<Subject> findByBoosterVaccinationDate(
            @LookupField(name = "boosterVaccinationDate") LocalDate dateRange);

    @Lookup(name = "Find By Participant Id")
    List<Subject> findByMatchesCaseInsensitiveSubjectId(@LookupField(name = "subjectId",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup(name = "Find By exact Phone Number")
    List<Subject> findByPhoneNumber(@LookupField(name = "phoneNumber") String phoneNumber);

    @Lookup(name = "Find By Visit Type and Actual Date")
    List<Subject> findByVisitTypeAndActualDate(
            @LookupField(name = "visits.type") VisitType visitType,
            @LookupField(name = "visits.date", customOperator = Constants.Operators.NEQ) LocalDate date);

    @Lookup(name = "Find by Visit Type and Actual Date Range")
    List<Subject> findSubjectByVisitTypeAndActualDateRange(
            @LookupField(name = "visits.type") VisitType visitType,
            @LookupField(name = "visits.date")  Range<LocalDate> date);
}
