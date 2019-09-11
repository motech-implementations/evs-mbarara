package org.motechproject.evsmbarara.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.commons.date.model.Time;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.dto.PrimeVaccinationScheduleDto;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.impl.PrimeVaccinationScheduleServiceImpl;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PrimeVaccinationScheduleService.class)
public class PrimeVaccinatonScheduleServiceTest {

    private static final String SITE_ID = "siteId";
    private static final String SITE_LOCATION = "site";
    private static final String SUBJECT_ID = "subjectId";

    @InjectMocks
    private PrimeVaccinationScheduleService primeVaccinationScheduleService = new PrimeVaccinationScheduleServiceImpl();

    @Mock
    private SubjectDataService subjectDataService;

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private ClinicDataService clinicDataService;

    private Subject subject;

    private Clinic clinic;

    @Before
    public void setUp() {
        initMocks(this);
        subject = createSubject(SUBJECT_ID, SITE_ID, SITE_LOCATION);
        clinic = new Clinic();
        clinic.setId(1L);
        clinic.setLocation(SITE_LOCATION);
        clinic.setSiteId(SITE_ID);
    }

    @Test
    public void shouldCreatePrimeVaccinationRecord() {
        subject.setVisits(new ArrayList<Visit>());

        Visit expectedPrimeVac = createVisit(subject, null, VisitType.PRIME_VACCINATION_DAY, null, new LocalDate(2217, 1, 28));
        expectedPrimeVac.setStartTime(new Time(9, 0));
        expectedPrimeVac.setEndTime(new Time(10, 0));
        expectedPrimeVac.setIgnoreDateLimitation(true);
        expectedPrimeVac.getSubject().setFemaleChildBearingAge(false);

        PrimeVaccinationScheduleDto primeVaccinationScheduleDto = new PrimeVaccinationScheduleDto(expectedPrimeVac);
        primeVaccinationScheduleDto.setIgnoreDateLimitation(true);
        primeVaccinationScheduleDto.setStartTime(new Time(9, 0));
        primeVaccinationScheduleDto.setActualScreeningDate(new LocalDate(2217, 1, 14));

        when(visitDataService.findById(primeVaccinationScheduleDto.getVisitId())).thenReturn(null);
        when(clinicDataService.findByExactSiteId(SITE_ID)).thenReturn(clinic);
        when(subjectDataService.findBySubjectId(SUBJECT_ID)).thenReturn(subject);
        when(visitDataService.create(any(Visit.class))).thenReturn(expectedPrimeVac);

        primeVaccinationScheduleService.createOrUpdateWithDto(primeVaccinationScheduleDto, true);

        ArgumentCaptor<Visit> visitBookingDetailsArgumentCaptor = ArgumentCaptor.forClass(Visit.class);
        verify(visitDataService, times(2)).create(visitBookingDetailsArgumentCaptor.capture());

        Visit primeVacAdded = visitBookingDetailsArgumentCaptor.getAllValues().get(1);

        assertEquals(expectedPrimeVac.getId(), primeVacAdded.getId());
        assertEquals(expectedPrimeVac.getDateProjected(), primeVacAdded.getDateProjected());
        assertEquals(expectedPrimeVac.getStartTime(), primeVacAdded.getStartTime());
        assertEquals(expectedPrimeVac.getEndTime(), primeVacAdded.getEndTime());
        assertEquals(expectedPrimeVac.getIgnoreDateLimitation(), primeVacAdded.getIgnoreDateLimitation());
        assertEquals(expectedPrimeVac.getSubject().getFemaleChildBearingAge(), primeVacAdded.getSubject().getFemaleChildBearingAge());
    }

    @Test
    public void shouldUpdatePrimeVaccinationRecord() {
        List<Visit> visits = new ArrayList<>();
        visits.add(createVisit(subject, 2L, VisitType.SCREENING, new LocalDate(2217, 1, 1), new LocalDate(2216, 1, 1)));
        Visit visit = createVisit(subject, 1L, VisitType.PRIME_VACCINATION_DAY, null, new LocalDate(2217, 1, 28));
        visits.add(visit);

        subject.setVisits(visits);

        PrimeVaccinationScheduleDto primeVaccinationScheduleDto = new PrimeVaccinationScheduleDto(visit);
        primeVaccinationScheduleDto.setIgnoreDateLimitation(true);
        primeVaccinationScheduleDto.setVisitId(1L);

        when(visitDataService.findById(primeVaccinationScheduleDto.getVisitId())).thenReturn(visit);

        // Update visit
        primeVaccinationScheduleDto.setStartTime(new Time(12, 0));
        primeVaccinationScheduleDto.setDate(new LocalDate(2217, 1, 2));
        primeVaccinationScheduleDto.setFemaleChildBearingAge(false);
        Visit expectedPrimeVac = new Visit();
        expectedPrimeVac.setId(1L);
        expectedPrimeVac.setStartTime(primeVaccinationScheduleDto.getStartTime());
        expectedPrimeVac.setEndTime(new Time(13, 0));
        expectedPrimeVac.setIgnoreDateLimitation(true);
        expectedPrimeVac.setSubject(subject);
        expectedPrimeVac.getSubject().setFemaleChildBearingAge(primeVaccinationScheduleDto.getFemaleChildBearingAge());
        expectedPrimeVac.setDateProjected(primeVaccinationScheduleDto.getDate());

        when(visitDataService.update(any(Visit.class))).thenReturn(expectedPrimeVac);
        when(clinicDataService.findByExactSiteId(SITE_ID)).thenReturn(clinic);
        when(subjectDataService.findBySubjectId(SUBJECT_ID)).thenReturn(subject);

        primeVaccinationScheduleService.createOrUpdateWithDto(primeVaccinationScheduleDto, true);

        ArgumentCaptor<Visit> visitBookingDetailsArgumentCaptor = ArgumentCaptor.forClass(Visit.class);
        verify(visitDataService, times(2)).update(visitBookingDetailsArgumentCaptor.capture());

        Visit primeVacUpdated = visitBookingDetailsArgumentCaptor.getAllValues().get(1);

        assertEquals(expectedPrimeVac.getId(), primeVacUpdated.getId());
        assertEquals(expectedPrimeVac.getDateProjected(), primeVacUpdated.getDateProjected());
        assertEquals(expectedPrimeVac.getStartTime(), primeVacUpdated.getStartTime());
        assertEquals(expectedPrimeVac.getEndTime(), primeVacUpdated.getEndTime());
        assertEquals(expectedPrimeVac.getIgnoreDateLimitation(), primeVacUpdated.getIgnoreDateLimitation());
        assertEquals(expectedPrimeVac.getSubject().getFemaleChildBearingAge(), primeVacUpdated.getSubject().getFemaleChildBearingAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenDateIsToLate() {
        PrimeVaccinationScheduleDto primeVaccinationScheduleDto = new PrimeVaccinationScheduleDto();
        primeVaccinationScheduleDto.setIgnoreDateLimitation(false);
        primeVaccinationScheduleDto.setDate(new LocalDate(2217, 2, 4));
        primeVaccinationScheduleDto.setActualScreeningDate(new LocalDate(2217, 1, 2));

        primeVaccinationScheduleService.createOrUpdateWithDto(primeVaccinationScheduleDto, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenDateIsToEarly() {
        PrimeVaccinationScheduleDto primeVaccinationScheduleDto = new PrimeVaccinationScheduleDto();
        primeVaccinationScheduleDto.setIgnoreDateLimitation(false);
        primeVaccinationScheduleDto.setDate(new LocalDate(2217, 1, 1));
        primeVaccinationScheduleDto.setActualScreeningDate(new LocalDate(2217, 1, 2));

        primeVaccinationScheduleService.createOrUpdateWithDto(primeVaccinationScheduleDto, false);
    }

    @Test
    public void shouldGetPrimeVaccinationRecords() {
        List<Visit> visits = new ArrayList<>();
        visits.add(new Visit());

        // This subject has visits so this students's DTO shouldn't be created
        subject.setVisits(visits);

        Subject subjectWithNoVisit = createSubject("subject2", SITE_ID, SITE_LOCATION);

        when(subjectDataService.findByPrimerVaccinationDate(null)).thenReturn(Arrays.asList(subject, subjectWithNoVisit));

        List<PrimeVaccinationScheduleDto> resultDtos = primeVaccinationScheduleService.getPrimeVaccinationScheduleRecords();

        assertEquals(1, resultDtos.size());
        assertEquals(subjectWithNoVisit.getSubjectId(), resultDtos.get(0).getParticipantId());
    }

    private Visit createVisit(Subject subject, Long id, VisitType visitType, LocalDate date, LocalDate projectedDate) {
        Visit visit = new Visit();
        visit.setDate(date);
        visit.setDateProjected(projectedDate);
        visit.setType(visitType);
        visit.setId(id);
        visit.setSubject(subject);
        return visit;
    }

    private Subject createSubject(String subjectId, String siteId, String siteName) {
        Subject newSubject = new Subject();
        newSubject.setSubjectId(subjectId);
        newSubject.setName("name");
        newSubject.setGender(Gender.Male);
        newSubject.setPhoneNumber("123456789");
        newSubject.setAddress("address");
        newSubject.setCommunity("community");
        newSubject.setSiteId(siteId);
        newSubject.setSiteName(siteName);
        newSubject.setChiefdom("chiefom");
        newSubject.setSection("section");
        newSubject.setDistrict("district");
        newSubject.setLanguage(Language.English);
        newSubject.setVisits(new ArrayList<Visit>());
        return newSubject;
    }

}
