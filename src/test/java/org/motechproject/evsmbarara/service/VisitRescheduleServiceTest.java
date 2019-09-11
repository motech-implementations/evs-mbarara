package org.motechproject.evsmbarara.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.commons.api.Range;
import org.motechproject.evsmbarara.domain.Config;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.VisitScheduleOffset;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.dto.VisitRescheduleDto;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.impl.VisitRescheduleServiceImpl;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;
import org.motechproject.mds.query.QueryParams;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VisitRescheduleServiceImpl.class)
public class VisitRescheduleServiceTest {


    @InjectMocks
    private VisitRescheduleService visitRescheduleService = new VisitRescheduleServiceImpl();

    @Mock
    private ConfigService configService;

    @Mock
    private VisitScheduleOffsetService visitScheduleOffsetService;

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private LookupService lookupService;

    @Mock
    private SubjectDataService subjectDataService;

    private Subject subject;

    @Before
    public void setUp() {
        initMocks(this);
        subject = createSubject();
        subject.setPrimerVaccinationDate(new LocalDate(2217, 2, 1));
        subject.setBoosterVaccinationDate(new LocalDate(2217, 3, 1));
    }

    @Test
    public void shouldGetVisitRecords() throws IOException {
        GridSettings gridSettings = new GridSettings();
        gridSettings.setPage(1);
        gridSettings.setRows(10);

        List<Visit> visits = new ArrayList<>(Arrays.asList(
                createVisit(1L, VisitType.D1_VISIT, null, new LocalDate(2217, 4, 1), subject),
                createVisit(2L, VisitType.D7_VISIT, null, new LocalDate(2217, 4, 1), subject)
        ));

        Records<Visit> records = new Records<>(1, 10, 2, visits);

        when(lookupService.getEntities(eq(Visit.class), anyString(), anyString(), any(QueryParams.class))).thenReturn(records);

        List<VisitScheduleOffset> visitScheduleOffsets = new ArrayList<>(Arrays.asList(
                createVisitScheduleOffset(VisitType.D1_VISIT, 10, 5, 12),
                createVisitScheduleOffset(VisitType.D7_VISIT, 20, 15, 24)
        ));
        Map<VisitType, VisitScheduleOffset> offsetMap = new LinkedHashMap<>();
        offsetMap.put(VisitType.D1_VISIT, visitScheduleOffsets.get(0));
        offsetMap.put(VisitType.D7_VISIT, visitScheduleOffsets.get(1));

        when(visitScheduleOffsetService.getAllAsMap()).thenReturn(offsetMap);

        Config config = new Config();

        List<String> boosterRelatedVisits = new ArrayList<>(Collections.singletonList("D63 Boost Vaccination First Follow-up Visit"));
        config.setBoosterRelatedVisits(boosterRelatedVisits);

        when(configService.getConfig()).thenReturn(config);

        List<VisitRescheduleDto> expectedDtos = new ArrayList<>(Arrays.asList(
                new VisitRescheduleDto(visits.get(0), new Range<>(new LocalDate(2217, 2, 6), new LocalDate(2217, 2, 13)), false, false),
                new VisitRescheduleDto(visits.get(1), new Range<>(new LocalDate(2217, 3, 16), new LocalDate(2217, 3, 25)), true, false)
        ));

        List<VisitRescheduleDto> resultDtos = visitRescheduleService.getVisitsRecords(gridSettings).getRows();

        checkVisitRescheduleDtoList(expectedDtos, resultDtos);
    }

    @Test
    public void shouldNotCalculateVisitDateRangeIfVisitScheduleOffsetIsMissing() throws IOException {
        List<Visit> visits = new ArrayList<>(Arrays.asList(
                createVisit(1L, VisitType.D57_VISIT, null, null, subject),
                createVisit(2L, VisitType.D59_VISIT, null, null, subject)
        ));

        Records<Visit> records = new Records<>(1, 10, visits.size(), visits);

        when(lookupService.getEntities(eq(Visit.class), anyString(), anyString(), any(QueryParams.class))).thenReturn(records);

        when(visitScheduleOffsetService.getAllAsMap()).thenReturn(new HashMap<VisitType, VisitScheduleOffset>());

        Config config = new Config();

        when(configService.getConfig()).thenReturn(config);

        List<VisitRescheduleDto> expectedDtos = new ArrayList<>(Arrays.asList(
                new VisitRescheduleDto(visits.get(0), null, false, false),
                new VisitRescheduleDto(visits.get(1), null, false, false)
        ));

        List<VisitRescheduleDto> resultDtos = visitRescheduleService.getVisitsRecords(new GridSettings()).getRows();

        checkVisitRescheduleDtoList(expectedDtos, resultDtos);
    }

    @Test
    public void shouldSaveRescheduledVisit() {
        Visit visit = createVisit(1L, VisitType.D1_VISIT, null, new LocalDate(2217, 1, 1), subject);

        VisitRescheduleDto expectedVisitRescheduleDto = new VisitRescheduleDto(visit, new Range<>(new LocalDate(2217, 2, 1), new LocalDate(2217, 3, 1)), false, false);
        Boolean ignoreLimitation = true;
        expectedVisitRescheduleDto.setPlannedDate(new LocalDate(2217, 1, 2));
        expectedVisitRescheduleDto.setIgnoreDateLimitation(ignoreLimitation);
        expectedVisitRescheduleDto.setVisitId(1L);

        when(visitDataService.findById(1L)).thenReturn(visit);
        when(visitDataService.update(visit)).thenReturn(visit);

        VisitRescheduleDto actualVisitRescheduleDto = visitRescheduleService.saveVisitReschedule(expectedVisitRescheduleDto, ignoreLimitation);

        // Returned dto should not have earliest and latest return dates
        expectedVisitRescheduleDto.setEarliestDate(null);
        expectedVisitRescheduleDto.setLatestDate(null);

        assertVisitRescheduleDto(expectedVisitRescheduleDto, actualVisitRescheduleDto);

        verify(visitDataService).update(any(Visit.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenVisitActualDateIsInFuture() {
        Visit visit = createVisit(1L, VisitType.D7_VISIT,
                new LocalDate().plusDays(3), new LocalDate(2217, 1, 1), subject);

        VisitRescheduleDto visitRescheduleDto = new VisitRescheduleDto(visit);

        when(visitDataService.findById(1L)).thenReturn(visit);

        visitRescheduleService.saveVisitReschedule(visitRescheduleDto, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenPlannedDateIsInThePast() {
        Visit visit = createVisit(1L, VisitType.D7_VISIT, null, new LocalDate(2015, 1, 1), subject);

        VisitRescheduleDto visitRescheduleDto = new VisitRescheduleDto(visit);
        visitRescheduleDto.setPlannedDate(new LocalDate(2015, 1, 2));

        when(visitDataService.findById(1L)).thenReturn(visit);

        visitRescheduleService.saveVisitReschedule(visitRescheduleDto, true);
    }

    @Test
    public void shouldNotThrowIllegalArgumentExceptionWhenPlannedDateIsInThePastButNotChanged() {
        LocalDate plannedDateInPast = new LocalDate(2015, 1, 1);
        Visit visit = createVisit(1L, VisitType.D7_VISIT, null, plannedDateInPast, subject);

        VisitRescheduleDto expectedVisitRescheduleDto = new VisitRescheduleDto(visit, new Range<>(new LocalDate(2217, 2, 1), new LocalDate(2217, 3, 1)), false, false);
        expectedVisitRescheduleDto.setPlannedDate(plannedDateInPast);
        expectedVisitRescheduleDto.setIgnoreDateLimitation(true);

        when(visitDataService.findById(1L)).thenReturn(visit);
        when(visitDataService.update(visit)).thenReturn(visit);

        VisitRescheduleDto actualVisitRescheduleDto = visitRescheduleService.saveVisitReschedule(expectedVisitRescheduleDto, true);

        // Returned dto should not have earliest and latest return dates
        expectedVisitRescheduleDto.setEarliestDate(null);
        expectedVisitRescheduleDto.setLatestDate(null);

        assertVisitRescheduleDto(expectedVisitRescheduleDto, actualVisitRescheduleDto);

        verify(visitDataService).update(any(Visit.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenCannotCalculateVisitWindow() {
        Visit visit = createVisit(1L, VisitType.D1_VISIT, null, new LocalDate(2217, 1, 1), subject);

        VisitRescheduleDto visitRescheduleDto = new VisitRescheduleDto(visit);
        visitRescheduleDto.setIgnoreDateLimitation(false);
        visitRescheduleDto.setVisitId(1L);

        when(visitDataService.findById(1L)).thenReturn(visit);
        when(visitDataService.update(visit)).thenReturn(visit);

        Config config = new Config();

        when(configService.getConfig()).thenReturn(config);

        visitRescheduleService.saveVisitReschedule(visitRescheduleDto, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenVisitPlannedDateOutOfWindow() {
        Visit visit = createVisit(1L, VisitType.D7_VISIT, null, new LocalDate(2217, 1, 1), subject);

        VisitRescheduleDto visitRescheduleDto = new VisitRescheduleDto(visit);
        visitRescheduleDto.setIgnoreDateLimitation(false);
        visitRescheduleDto.setVisitId(1L);

        when(visitDataService.findById(1L)).thenReturn(visit);
        when(visitDataService.update(visit)).thenReturn(visit);

        Map<VisitType, VisitScheduleOffset> offsetMap = new LinkedHashMap<>();
        offsetMap.put(VisitType.D7_VISIT, createVisitScheduleOffset(VisitType.D7_VISIT, 10, 5, 15));

        when(visitScheduleOffsetService.getAllAsMap()).thenReturn(offsetMap);

        Config config = new Config();

        when(configService.getConfig()).thenReturn(config);

        visitRescheduleService.saveVisitReschedule(visitRescheduleDto, true);
    }

    @Test
    public void shouldUpdateSubjectBoosterVaccinationDate() {
        Visit visit = createVisit(1L, VisitType.BOOST_VACCINATION_DAY, null, new LocalDate(2217, 1, 1), subject);

        VisitRescheduleDto expectedVisitRescheduleDto = new VisitRescheduleDto(visit, new Range<>(new LocalDate(2217, 2, 1), new LocalDate(2217, 3, 1)), false, false);
        Boolean ignoreLimitation = true;
        expectedVisitRescheduleDto.setPlannedDate(new LocalDate(2217, 1, 2));
        expectedVisitRescheduleDto.setIgnoreDateLimitation(ignoreLimitation);
        expectedVisitRescheduleDto.setVisitId(1L);

        when(visitDataService.findById(1L)).thenReturn(visit);
        when(visitDataService.update(visit)).thenReturn(visit);

        VisitRescheduleDto actualVisitRescheduleDto = visitRescheduleService.saveVisitReschedule(expectedVisitRescheduleDto, ignoreLimitation);

        // Returned dto should not have earliest and latest return dates
        expectedVisitRescheduleDto.setEarliestDate(null);
        expectedVisitRescheduleDto.setLatestDate(null);
        assertVisitRescheduleDto(expectedVisitRescheduleDto, actualVisitRescheduleDto);

        verify(subjectDataService).update(any(Subject.class));
        verify(visitDataService).update(any(Visit.class));
    }

    private VisitScheduleOffset createVisitScheduleOffset(VisitType visitType, Integer timeOffset, Integer earliestDateOffset, Integer latestDateOffset) {
        VisitScheduleOffset visitScheduleOffset = new VisitScheduleOffset();
        visitScheduleOffset.setVisitType(visitType);
        visitScheduleOffset.setTimeOffset(timeOffset);
        visitScheduleOffset.setEarliestDateOffset(earliestDateOffset);
        visitScheduleOffset.setLatestDateOffset(latestDateOffset);
        return visitScheduleOffset;
    }

    private Visit createVisit(Long id, VisitType visitType, LocalDate date, LocalDate dateProjected, Subject subject) {
        Visit visit = new Visit();
        visit.setId(id);
        visit.setType(visitType);
        visit.setDate(date);
        visit.setDateProjected(dateProjected);
        visit.setSubject(subject);

        subject.getVisits().add(visit);

        return visit;
    }

    private void checkVisitRescheduleDtoList(List<VisitRescheduleDto> expectedDtos, List<VisitRescheduleDto> resultDtos) {
        for (int i = 0; i < expectedDtos.size(); i++) {
            assertVisitRescheduleDto(expectedDtos.get(i), resultDtos.get(i));
        }
    }

    private void assertVisitRescheduleDto(VisitRescheduleDto expected, VisitRescheduleDto actual) {
        assertEquals(expected.getEarliestDate(), actual.getEarliestDate());
        assertEquals(expected.getLatestDate(), actual.getLatestDate());
        assertEquals(expected.getParticipantId(), actual.getParticipantId());
        assertEquals(expected.getIgnoreDateLimitation(), actual.getIgnoreDateLimitation());
    }

    private Subject createSubject() {
        Subject newSubject = new Subject();
        newSubject.setSubjectId("1");
        newSubject.setName("asd");
        newSubject.setPhoneNumber("123");
        newSubject.setLanguage(Language.English);
        return newSubject;
    }
}
