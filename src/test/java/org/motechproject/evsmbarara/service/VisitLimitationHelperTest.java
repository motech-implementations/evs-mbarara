package org.motechproject.evsmbarara.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.exception.LimitationExceededException;
import org.motechproject.evsmbarara.helper.VisitLimitationHelper;
import org.motechproject.evsmbarara.repository.ScreeningDataService;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class VisitLimitationHelperTest {

    private static final int MAX_PRIME_VAC_DAY = 7;
    private static final int MAX_PRIME_FIRST_FOLLOW_UP = 8;
    private static final int MAX_PRIME_SECOND_FOLLOW_UP = 9;
    private static final int MAX_PRIME_THIRD_FOLLOW_UP = 10;
    private static final int MAX_BOOSTER_VAC = 11;
    private static final int MAX_BOOSTER_FIRST_FOLLOW_UP = 12;
    private static final int MAX_THREE_MONTHS_POST_PRIME = 13;
    private static final int MAX_SIX_MONTHS_POST_PRIME = 14;
    private static final int MAX_TWELVE_MONTHS_POST_PRIME = 15;
    private static final int MAX_TWENTY_FOUR_MONTHS_POST_PRIME = 16;
    private static final int MAX_THIRTY_SIX_MONTHS_POST_PRIME = 17;
    private static final int MAX_FORTY_EIGHT_MONTHS_POST_PRIME = 18;
    private static final int MAX_SIXTY_MONTHS_POST_PRIME = 19;

    @InjectMocks
    private VisitLimitationHelper visitLimitationHelper = new VisitLimitationHelper();

    @Mock
    private ScreeningDataService screeningDataService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Mock
    private VisitDataService visitDataService;

    @Rule
    public ExpectedException thrown = ExpectedException.none(); //NO CHECKSTYLE VisibilityModifier

    private Clinic clinic;

    @Before
    public void setUp() {
        initMocks(this);
        clinic = createClinic();
        clinic.setId(1L);

    }

    @Test(expected = LimitationExceededException.class)
    public void shouldThrowLimitationExceededExceptionWhenCapacityLimitIsReached() {
        when(screeningDataService.countFindByClinicIdDateAndScreeningIdAndStatus(new LocalDate(2217, 1, 1), clinic.getId(), 1L, ScreeningStatus.ACTIVE)).thenReturn(2L);
        when(unscheduledVisitDataService.countFindByClinicIdAndDateAndVisitId(new LocalDate(2217, 1, 1), clinic.getId(), null)).thenReturn(1L);
        when(visitDataService.countFindByVisitPlannedDateAndClinicIdAndVisitId(new LocalDate(2217, 1, 1), clinic.getId(), null)).thenReturn(4L);

        visitLimitationHelper.checkCapacityForScreening(new LocalDate(2217, 1, 1), clinic, 1L);
    }

    @Test
    public void shouldNotThrowLimitationExceededExceptionWhenCapacityLimitIsNotReached() {
        when(screeningDataService.countFindByClinicIdDateAndScreeningIdAndStatus(new LocalDate(2217, 1, 1), clinic.getId(), 1L, ScreeningStatus.ACTIVE)).thenReturn(2L);
        when(unscheduledVisitDataService.countFindByClinicIdAndDateAndVisitId(new LocalDate(2217, 1, 1), clinic.getId(), null)).thenReturn(1L);
        when(visitDataService.countFindByVisitPlannedDateAndClinicIdAndVisitId(new LocalDate(2217, 1, 1), clinic.getId(), null)).thenReturn(1L);

        visitLimitationHelper.checkCapacityForScreening(new LocalDate(2217, 1, 1), clinic, 1L);
    }

    @Test
    public void shouldReturnMaxVisitCountForVisitType() {
        assertEquals(MAX_PRIME_VAC_DAY, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.PRIME_VACCINATION_DAY, clinic));
        assertEquals(MAX_PRIME_FIRST_FOLLOW_UP, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT, clinic));
        assertEquals(MAX_PRIME_SECOND_FOLLOW_UP, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.PRIME_VACCINATION_SECOND_FOLLOW_UP_VISIT, clinic));
        assertEquals(MAX_PRIME_THIRD_FOLLOW_UP, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.PRIME_VACCINATION_THIRD_FOLLOW_UP_VISIT, clinic));
        assertEquals(MAX_BOOSTER_VAC, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.BOOST_VACCINATION_DAY, clinic));
        assertEquals(MAX_BOOSTER_FIRST_FOLLOW_UP, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT, clinic));
        assertEquals(MAX_THREE_MONTHS_POST_PRIME, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.THREE_MONTHS_POST_PRIME_VISIT, clinic));
        assertEquals(MAX_SIX_MONTHS_POST_PRIME, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.SIX_MONTHS_POST_PRIME_VISIT, clinic));
        assertEquals(MAX_TWELVE_MONTHS_POST_PRIME, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.TWELVE_MONTHS_POST_PRIME_VISIT, clinic));
        assertEquals(MAX_TWENTY_FOUR_MONTHS_POST_PRIME, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.TWENTY_FOUR_MONTHS_POST_PRIME_VISIT, clinic));
        assertEquals(MAX_THIRTY_SIX_MONTHS_POST_PRIME, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.THIRTY_SIX_MONTHS_POST_PRIME_VISIT, clinic));
        assertEquals(MAX_FORTY_EIGHT_MONTHS_POST_PRIME, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.FORTY_EIGHT_MONTHS_POST_PRIME_VISIT, clinic));
        assertEquals(MAX_SIXTY_MONTHS_POST_PRIME, visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.SIXTY_MONTHS_POST_PRIME_VISIT, clinic));

        thrown.expect(IllegalArgumentException.class);
        visitLimitationHelper.getMaxVisitCountForVisitType(VisitType.SCREENING, clinic);
    }

    private Clinic createClinic() {
        Clinic newClinic = new Clinic();
        newClinic.setSiteId("siteId");
        newClinic.setLocation("location");
        newClinic.setNumberOfRooms(1);
        newClinic.setMaxCapacityByDay(5);
        newClinic.setMaxScreeningVisits(1);
        newClinic.setMaxPrimeVisits(MAX_PRIME_VAC_DAY);
        newClinic.setMaxPrimeFirstFollowUpVisits(MAX_PRIME_FIRST_FOLLOW_UP);
        newClinic.setMaxPrimeSecondFollowUpVisits(MAX_PRIME_SECOND_FOLLOW_UP);
        newClinic.setMaxPrimeThirdFollowUpVisits(MAX_PRIME_THIRD_FOLLOW_UP);
        newClinic.setMaxBoosterVisits(MAX_BOOSTER_VAC);
        newClinic.setMaxBoosterFirstFollowUpVisits(MAX_BOOSTER_FIRST_FOLLOW_UP);
        newClinic.setMaxThreeMonthsPostPrimeVisits(MAX_THREE_MONTHS_POST_PRIME);
        newClinic.setMaxSixMonthsPostPrimeVisits(MAX_SIX_MONTHS_POST_PRIME);
        newClinic.setMaxTwelveMonthsPostPrimeVisits(MAX_TWELVE_MONTHS_POST_PRIME);
        newClinic.setMaxTwentyFourMonthsPostPrimeVisits(MAX_TWENTY_FOUR_MONTHS_POST_PRIME);
        newClinic.setMaxThirtySixMonthsPostPrimeVisits(MAX_THIRTY_SIX_MONTHS_POST_PRIME);
        newClinic.setMaxFortyEightMonthsPostPrimeVisits(MAX_FORTY_EIGHT_MONTHS_POST_PRIME);
        newClinic.setMaxSixtyMonthsPostPrimeVisits(MAX_SIXTY_MONTHS_POST_PRIME);
        return newClinic;
    }
}
