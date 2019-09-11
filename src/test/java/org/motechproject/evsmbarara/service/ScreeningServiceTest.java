package org.motechproject.evsmbarara.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;
import org.motechproject.evsmbarara.dto.ScreeningDto;
import org.motechproject.evsmbarara.domain.Volunteer;
import org.motechproject.evsmbarara.exception.LimitationExceededException;
import org.motechproject.evsmbarara.helper.VisitLimitationHelper;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.ScreeningDataService;
import org.motechproject.evsmbarara.repository.VolunteerDataService;
import org.motechproject.evsmbarara.service.impl.ScreeningServiceImpl;
import org.motechproject.commons.date.model.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;

public class ScreeningServiceTest {

    @InjectMocks
    private ScreeningService screeningService = new ScreeningServiceImpl();

    @Mock
    private ScreeningDataService screeningDataService;

    @Mock
    private VolunteerDataService volunteerDataService;

    @Mock
    private ClinicDataService clinicDataService;

    @Mock
    private VisitLimitationHelper visitLimitationHelper;

    @Rule
    public ExpectedException thrown = ExpectedException.none(); //NO CHECKSTYLE VisibilityModifier

    private Clinic clinic;

    @Before
    public void setUp() {
        initMocks(this);
        clinic = createClinic();
        clinic.setId(1L);
    }

    @Test
    public void shouldThrowLimitationExceededExceptionWhenNumberOfRoomsLimitIsReached() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(11, 0), new Time(12, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "11:00"); // todo Id may be unwanted here
        Screening screening = new Screening();
        screening.setId(1L);

        thrown.expect(LimitationExceededException.class);
        thrown.expectMessage("Too many visits at the same time");
        screeningService.addOrUpdate(screeningDto, false);

    }

    @Test
    public void shouldThrowLimitationExceededExceptionWhenTypeOfVisitLimitIsReached() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(10, 0), new Time(11, 0)),
                createScreeningVisit(3L, clinic, new LocalDate(2217, 1, 1), new Time(9, 0), new Time(10, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "12:00");
        Screening screening = new Screening();
        screening.setId(1L);

        thrown.expect(LimitationExceededException.class);
        thrown.expectMessage("The booking limit for this type of visit has been reached");
        screeningService.addOrUpdate(screeningDto, false);
    }

    @Test
    public void shouldCheckCapacityLimitFirst() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(10, 0), new Time(11, 0)),
                createScreeningVisit(3L, clinic, new LocalDate(2217, 1, 1), new Time(9, 0), new Time(10, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        doThrow(new LimitationExceededException("The clinic capacity limit for this day has been reached")).when(visitLimitationHelper).checkCapacityForScreening(any(LocalDate.class), any(Clinic.class), anyLong());

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "12:00");
        Screening screening = new Screening();
        screening.setId(1L);

        thrown.expect(LimitationExceededException.class);
        thrown.expectMessage("The clinic capacity limit for this day has been reached");
        screeningService.addOrUpdate(screeningDto, false);
    }

    @Test
    public void shouldNotCheckLimitationWhenIgnoreLimitationVariableIsTrue() {
        when(clinicDataService.findById(1L)).thenReturn(clinic);
        when(volunteerDataService.create(any(Volunteer.class))).thenReturn(new Volunteer());
        List<Screening> screenings = new ArrayList<>(Arrays.asList(
                createScreeningVisit(2L, clinic, new LocalDate(2217, 1, 1), new Time(10, 0), new Time(11, 0)),
                createScreeningVisit(3L, clinic, new LocalDate(2217, 1, 1), new Time(9, 0), new Time(10, 0))
        ));

        when(screeningDataService.findByClinicIdAndDateAndStatus(1L, new LocalDate(2217, 1, 1), ScreeningStatus.ACTIVE)).thenReturn(screenings);

        ScreeningDto screeningDto = createScreeningDto(null, "1", "2217-1-1", "12:00");
        Screening screening = new Screening();
        screening.setId(1L);

        screeningService.addOrUpdate(screeningDto, true);

        verify(visitLimitationHelper, never()).checkCapacityForScreening(any(LocalDate.class), any(Clinic.class), anyLong());
        verify(screeningDataService, times(1)).create(any(Screening.class));
    }

    private Screening createScreeningVisit(Long id, Clinic clinic, LocalDate date, Time startTime, Time endTime) {
        Screening screening = new Screening();
        screening.setId(id);
        screening.setClinic(clinic);
        screening.setDate(date);
        screening.setStartTime(startTime);
        screening.setEndTime(endTime);
        return screening;
    }

    private ScreeningDto createScreeningDto(String id, String clinicId, String date, String startTime) {
        ScreeningDto screeningDto = new ScreeningDto();
        screeningDto.setId(id);
        screeningDto.setClinicId(clinicId);
        screeningDto.setDate(date);
        screeningDto.setStartTime(startTime);
        return screeningDto;
    }

    private Clinic createClinic() {
        Clinic newClinic = new Clinic();
        newClinic.setSiteId("siteId");
        newClinic.setLocation("location");
        newClinic.setNumberOfRooms(1);
        newClinic.setMaxCapacityByDay(5);
        newClinic.setMaxScreeningVisits(2);
        newClinic.setMaxPrimeVisits(9);
        newClinic.setMaxPrimeFirstFollowUpVisits(8);
        newClinic.setMaxPrimeSecondFollowUpVisits(7);
        newClinic.setMaxPrimeThirdFollowUpVisits(10);
        newClinic.setMaxBoosterVisits(10);
        newClinic.setMaxBoosterFirstFollowUpVisits(10);
        newClinic.setMaxThreeMonthsPostPrimeVisits(10);
        newClinic.setMaxSixMonthsPostPrimeVisits(10);
        newClinic.setMaxTwelveMonthsPostPrimeVisits(10);
        newClinic.setMaxTwentyFourMonthsPostPrimeVisits(10);
        newClinic.setMaxThirtySixMonthsPostPrimeVisits(10);
        newClinic.setMaxFortyEightMonthsPostPrimeVisits(10);
        newClinic.setMaxSixtyMonthsPostPrimeVisits(10);
        return newClinic;
    }

}
