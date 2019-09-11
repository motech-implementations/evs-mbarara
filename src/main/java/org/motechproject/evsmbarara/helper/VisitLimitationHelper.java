package org.motechproject.evsmbarara.helper;

import org.joda.time.LocalDate;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.exception.LimitationExceededException;
import org.motechproject.evsmbarara.repository.ScreeningDataService;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VisitLimitationHelper {

    @Autowired
    private ScreeningDataService screeningDataService;

    @Autowired
    private VisitDataService visitDataService;

    @Autowired
    private UnscheduledVisitDataService unscheduledVisitDataService;

    public void checkCapacityForUnscheduleVisit(LocalDate date, Clinic clinic, Long id) {
        checkCapacity(date, clinic, null, id, null);
    }

    public void checkCapacityForScreening(LocalDate date, Clinic clinic, Long id) {
        checkCapacity(date, clinic, id, null, null);
    }

    public void checkCapacityForVisit(LocalDate date, Clinic clinic, Long id) {
        checkCapacity(date, clinic, null, null, id);
    }

    private void checkCapacity(LocalDate date, Clinic clinic, Long screeningId, Long unscheduledVisitId, Long visitId) {
        if (clinic != null && date != null) {
            int screeningCount = (int) screeningDataService.countFindByClinicIdDateAndScreeningIdAndStatus(date, clinic.getId(), screeningId, ScreeningStatus.ACTIVE);
            int unscheduledVisitCount = (int) unscheduledVisitDataService.countFindByClinicIdAndDateAndVisitId(date, clinic.getId(), unscheduledVisitId);
            int visitCount = (int) visitDataService.countFindByVisitPlannedDateAndClinicIdAndVisitId(date, clinic.getId(), visitId);
            int totalVisitCount = screeningCount + visitCount + unscheduledVisitCount;
            if (totalVisitCount >= clinic.getMaxCapacityByDay()) {
                throw new LimitationExceededException("The clinic capacity limit for this day has been reached");
            }
        }
    }

    public int getMaxVisitCountForVisitType(VisitType visitType, Clinic clinic) {  //NO CHECKSTYLE CyclomaticComplexity
        switch (visitType) {
            case PRIME_VACCINATION_DAY:
                return clinic.getMaxPrimeVisits();
            case PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT:
                return clinic.getMaxPrimeFirstFollowUpVisits();
            case PRIME_VACCINATION_SECOND_FOLLOW_UP_VISIT:
                return clinic.getMaxPrimeSecondFollowUpVisits();
            case PRIME_VACCINATION_THIRD_FOLLOW_UP_VISIT:
                return clinic.getMaxPrimeThirdFollowUpVisits();
            case BOOST_VACCINATION_DAY:
                return clinic.getMaxBoosterVisits();
            case BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT:
                return clinic.getMaxBoosterFirstFollowUpVisits();
            case THREE_MONTHS_POST_PRIME_VISIT:
                return clinic.getMaxThreeMonthsPostPrimeVisits();
            case SIX_MONTHS_POST_PRIME_VISIT:
                return clinic.getMaxSixMonthsPostPrimeVisits();
            case TWELVE_MONTHS_POST_PRIME_VISIT:
                return clinic.getMaxTwelveMonthsPostPrimeVisits();
            case TWENTY_FOUR_MONTHS_POST_PRIME_VISIT:
                return clinic.getMaxTwentyFourMonthsPostPrimeVisits();
            case THIRTY_SIX_MONTHS_POST_PRIME_VISIT:
                return clinic.getMaxThirtySixMonthsPostPrimeVisits();
            case FORTY_EIGHT_MONTHS_POST_PRIME_VISIT:
                return clinic.getMaxFortyEightMonthsPostPrimeVisits();
            case SIXTY_MONTHS_POST_PRIME_VISIT:
                return clinic.getMaxSixtyMonthsPostPrimeVisits();
            default:
                throw new IllegalArgumentException(String.format("Cannot find max visits number in Clinic for Visit Type: %s",
                        visitType.getDisplayValue()));
        }
    }
}
