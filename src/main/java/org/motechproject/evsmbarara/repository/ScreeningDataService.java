package org.motechproject.evsmbarara.repository;

import java.util.List;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;

public interface ScreeningDataService extends MotechDataService<Screening> {

    @Lookup
    List<Screening> findByDate(@LookupField(name = "date") Range<LocalDate> dateRange);

    @Lookup
    List<Screening> findByClinicIdDateAndScreeningIdAndStatus(
            @LookupField(name = "date") LocalDate date,
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id,
            @LookupField(name = "status") ScreeningStatus status);

    long countFindByClinicIdDateAndScreeningIdAndStatus(@LookupField(name = "date") LocalDate date,
                                                        @LookupField(name = "clinic.id") Long clinicId,
                                                        @LookupField(name = "id", customOperator = Constants.Operators.NEQ) Long id,
                                                        @LookupField(name = "status") ScreeningStatus status);

    @Lookup
    List<Screening> findByClinicLocationAndDate(@LookupField(name = "date") Range<LocalDate> date,
                                                @LookupField(name = "clinic.location", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<Screening> findByClinicLocation(
            @LookupField(name = "clinic.location", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<Screening> findByBookingId(@LookupField(name = "volunteer.id") Long bookingId);

    @Lookup
    List<Screening> findByBookingIdAndDate(@LookupField(name = "date") Range<LocalDate> date,
                                           @LookupField(name = "volunteer.id") Long bookingId);

    @Lookup
    List<Screening> findByClinicIdAndDateRangeAndStatus(
            @LookupField(name = "clinic.id") Long clinicId,
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "status") ScreeningStatus status);

    long countFindByClinicIdAndDateRangeAndStatus(@LookupField(name = "clinic.id") Long clinicId,
                                                  @LookupField(name = "date") Range<LocalDate> date,
                                                  @LookupField(name = "status") ScreeningStatus status);

    @Lookup
    List<Screening> findByClinicIdAndDateAndStatus(@LookupField(name = "clinic.id") Long clinicId,
                                                   @LookupField(name = "date") LocalDate date,
                                                   @LookupField(name = "status") ScreeningStatus status);

    long countFindByClinicIdAndDateAndStatus(@LookupField(name = "clinic.id") Long clinicId,
                                             @LookupField(name = "date") LocalDate date,
                                             @LookupField(name = "status") ScreeningStatus status);

    @Lookup
    List<Screening> findByName(
            @LookupField(name = "volunteer.name", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String volunteerName);

    @Lookup
    List<Screening> findByNameAndDate(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "volunteer.name", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String volunteerName);

    @Lookup
    List<Screening> findByContactNumber(
            @LookupField(name = "volunteer.contactNumber", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String volunteerContactNumber);

    @Lookup
    List<Screening> findByContactNumberAndDate(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "volunteer.contactNumber", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String volunteerContactNumber);

    @Lookup
    List<Screening> findByStatus(@LookupField(name = "status") ScreeningStatus status);

    @Lookup
    List<Screening> findByStatusAndDate(
        @LookupField(name = "status") ScreeningStatus status,
        @LookupField(name = "date") Range<LocalDate> date);
}
