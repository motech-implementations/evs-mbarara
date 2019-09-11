package org.motechproject.evsmbarara.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Config;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.VisitScheduleOffset;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.exception.VisitScheduleException;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.service.impl.VisitScheduleServiceImpl;

public class VisitScheduleServiceTest {

    private static final String PRIME_VAC_DATE_NAME = "primeVacDate";
    private static final String EARLIEST_DATE_NAME = "earliestDate";
    private static final String LATEST_DATE_NAME = "latestDate";

    @InjectMocks
    private VisitScheduleService visitScheduleService = new VisitScheduleServiceImpl();

    @Mock
    private SubjectDataService subjectDataService;

    @Mock
    private VisitScheduleOffsetService visitScheduleOffsetService;

    @Mock
    private ConfigService configService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldSetScreeningDateAsEarliestDate() {
        String subjectId = "subjectId";
        LocalDate screeningDate = new LocalDate(2017, 4, 15);

        Subject subject = createSubject(subjectId, null, false);
        setSubjectVisits(subject, screeningDate, null);

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);

        Map<String, String> resultMap = visitScheduleService.getPrimeVaccinationDateAndDateRange(subjectId);

        checkMap(resultMap, "", "2017-04-15", "2017-05-13");
    }

    @Test
    public void shouldGetPrimeVaccinationDateAndDateRange() {
        String subjectId = "subjectId";
        LocalDate screeningDate = new LocalDate(2017, 4, 15);
        LocalDate primeVacDate = new LocalDate(2017, 4, 17);

        Subject subject = createSubject(subjectId, primeVacDate, false);
        setSubjectVisits(subject, screeningDate, primeVacDate);

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);

        Map<String, String> resultMap = visitScheduleService.getPrimeVaccinationDateAndDateRange(subjectId);

        checkMap(resultMap, "2017-04-17", "2017-04-17", "2017-05-13");
    }

    @Test
    public void shouldGetPrimeVaccinationDateAndDateRangeChildBearingAge() {
        String subjectId = "subjectId";
        LocalDate screeningDate = new LocalDate(2017, 4, 15);
        LocalDate primeVacDate = new LocalDate(2017, 4, 17);

        Subject subject = createSubject(subjectId, primeVacDate, true);
        setSubjectVisits(subject, screeningDate, primeVacDate);

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);

        Map<String, String> resultMap = visitScheduleService.getPrimeVaccinationDateAndDateRange(subjectId);

        checkMap(resultMap, "2017-04-17", "2017-04-17", "2017-05-13");
    }

    @Test(expected = VisitScheduleException.class)
    public void shouldThrowExceptionWhenSubjectHasNoScreening() {
        String subjectId = "subjectId";
        LocalDate primeVacDate = new LocalDate(2017, 4, 17);

        Subject subject = createSubject(subjectId, primeVacDate, true);
        setSubjectVisits(subject, null, primeVacDate);

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);


        visitScheduleService.getPrimeVaccinationDateAndDateRange(subjectId);
    }

    @Test
    public void shouldCalculatePlannedDates() {
        String subjectId = "subjectId";
        LocalDate screeningDate = new LocalDate(2017, 4, 15);
        LocalDate primeVacDate = new LocalDate(2017, 4, 17);
        List<String> boosterRelated = new ArrayList<>(Collections.singletonList(VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT.getDisplayValue()));

        Subject subject = createSubject(subjectId, null, false);
        setSubjectVisits(subject, screeningDate, primeVacDate);

        Map<VisitType, VisitScheduleOffset> visitTypeVisitScheduleOffsetMap = createVisitScheduleOffsetMap();

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(visitScheduleOffsetService.getAllAsMap()).thenReturn(visitTypeVisitScheduleOffsetMap);
        Config config = mock(Config.class);
        when(configService.getConfig()).thenReturn(config);
        when(config.getBoosterRelatedVisits()).thenReturn(boosterRelated);

        Map<String, String> resultMap = visitScheduleService.calculatePlannedVisitDates(subjectId, primeVacDate, false);

        checkCalculatedVisits(createVisitScheduleOffsetMap(), resultMap, primeVacDate);
    }

    @Test(expected = VisitScheduleException.class)
    public void shouldThrowExceptionWhenPrimeVacDateIsEmpty() {
        String subjectId = "subjectId";
        LocalDate screeningDate = new LocalDate(2017, 4, 15);
        LocalDate primeVacDate = null;

        Subject subject = createSubject(subjectId, null, false);
        setSubjectVisits(subject, screeningDate, primeVacDate);

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);

        visitScheduleService.calculatePlannedVisitDates(subjectId, primeVacDate, false);
    }

    @Test(expected = VisitScheduleException.class)
    public void shouldThrowExceptionWhenVisitScheduleOffsetMapIsEmpty() {
        String subjectId = "subjectId";
        LocalDate screeningDate = new LocalDate(2017, 4, 15);
        LocalDate primeVacDate = new LocalDate(2017, 4, 17);

        Subject subject = createSubject(subjectId, null, false);
        setSubjectVisits(subject, screeningDate, primeVacDate);

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(visitScheduleOffsetService.getAllAsMap()).thenReturn(null);

        visitScheduleService.calculatePlannedVisitDates(subjectId, primeVacDate, false);

    }

    @Test(expected = VisitScheduleException.class)
    public void shouldThrowExceptionWhenPrimeOrScreeningVisitIsEmpty() {
        String subjectId = "subjectId";

        Subject subject = createSubject(subjectId, null, false);

        Map<VisitType, VisitScheduleOffset> visitTypeVisitScheduleOffsetMap = createVisitScheduleOffsetMap();

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(visitScheduleOffsetService.getAllAsMap()).thenReturn(visitTypeVisitScheduleOffsetMap);

        visitScheduleService.calculatePlannedVisitDates(subjectId, null, false);
    }

    @Test
    public void shouldSavePlannedVisitDates() {
        String subjectId = "subjectId";
        LocalDate screeningDate = new LocalDate(2017, 4, 15);
        LocalDate primeVacDate = new LocalDate(2017, 4, 17);
        List<String> boosterRelated = new ArrayList<>(Collections.singletonList(VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT.getDisplayValue()));

        Subject subject = createSubject(subjectId, null, false);
        setSubjectVisits(subject, screeningDate, primeVacDate);

        Map<VisitType, VisitScheduleOffset> visitTypeVisitScheduleOffsetMap = createVisitScheduleOffsetMap();

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(visitScheduleOffsetService.getAllAsMap()).thenReturn(visitTypeVisitScheduleOffsetMap);
        Config config = mock(Config.class);
        when(configService.getConfig()).thenReturn(config);
        when(config.getBoosterRelatedVisits()).thenReturn(boosterRelated);

        visitScheduleService.savePlannedVisitDates(subjectId, primeVacDate, false);

        ArgumentCaptor<Subject> subjectArgumentCaptor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectDataService).update(subjectArgumentCaptor.capture());

        Subject updatedSubject = subjectArgumentCaptor.getValue();

        assertEquals(updatedSubject.getPrimerVaccinationDate(), primeVacDate);
        checkVisitList(createVisitScheduleOffsetMap(), updatedSubject.getVisits(), primeVacDate);
    }

    private void checkVisitList(Map<VisitType, VisitScheduleOffset> offsetMap, List<Visit> visits, LocalDate baseDate) {
        List<VisitType> visitTypes = Arrays.asList(VisitType.values());
        assertEquals(visitTypes.size(), visits.size());

        for (Visit visit : visits) {
            if (visit.getType() != VisitType.SCREENING && visit.getType() != VisitType.PRIME_VACCINATION_DAY) {
                if (visit.getType().equals(VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT)) {
                    LocalDate boosterBaseDate = getVisitByType(visits, VisitType.BOOST_VACCINATION_DAY).getDateProjected();
                    LocalDate calculatedDate = visit.getDateProjected();

                    Integer timeOffset = offsetMap.get(visit.getType()).getTimeOffset();

                    assertEquals(boosterBaseDate.plusDays(timeOffset), calculatedDate);
                } else {
                    LocalDate calculatedDate = visit.getDateProjected();

                    Integer timeOffset = offsetMap.get(visit.getType()).getTimeOffset();

                    assertEquals(baseDate.plusDays(timeOffset), calculatedDate);
                }
            }
        }
    }

    private Visit getVisitByType(List<Visit> visits, VisitType type) {
        for (Visit v : visits) {
            if (v.getType().equals(type)) {
                return v;
            }
        }
        return null;
    }

    private void checkCalculatedVisits(Map<VisitType, VisitScheduleOffset> offsetMap, Map<String, String> calculatedVisits, LocalDate baseDate) {
        for (VisitType key : offsetMap.keySet()) {
            if (key.equals(VisitType.BOOST_VACCINATION_FIRST_FOLLOW_UP_VISIT)) {
                String boostPrimeVacDate = calculatedVisits.get(VisitType.BOOST_VACCINATION_DAY.getDisplayValue());
                LocalDate baseBoostDate = LocalDate.parse(boostPrimeVacDate, DateTimeFormat.forPattern(
                    EvsMbararaConstants.SIMPLE_DATE_FORMAT));
                String calculatedDateString = calculatedVisits.get(key.getDisplayValue());
                LocalDate calculatedDate = LocalDate.parse(calculatedDateString,  DateTimeFormat.forPattern(
                    EvsMbararaConstants.SIMPLE_DATE_FORMAT));
                Integer timeOffset = offsetMap.get(key).getTimeOffset();

                assertEquals(baseBoostDate.plusDays(timeOffset), calculatedDate);
            } else {
                String calculatedDateString = calculatedVisits.get(key.getDisplayValue());
                LocalDate calculatedDate = LocalDate.parse(calculatedDateString,  DateTimeFormat.forPattern(
                    EvsMbararaConstants.SIMPLE_DATE_FORMAT));
                Integer timeOffset = offsetMap.get(key).getTimeOffset();

                assertEquals(baseDate.plusDays(timeOffset), calculatedDate);
            }
        }
    }

    private void checkMap(Map<String, String> resultMap, String primeVacDate, String earliestDate, String latestDate) {
        assertEquals(primeVacDate, resultMap.get(PRIME_VAC_DATE_NAME));
        assertEquals(earliestDate, resultMap.get(EARLIEST_DATE_NAME));
        assertEquals(latestDate, resultMap.get(LATEST_DATE_NAME));
    }

    private Subject createSubject(String subjectId, LocalDate primeVacDate, boolean childBearingAge) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        subject.setPrimerVaccinationDate(primeVacDate);
        subject.setFemaleChildBearingAge(childBearingAge);
        return subject;
    }

    private void setSubjectVisits(Subject subject, LocalDate screeningDate, LocalDate primeVacDate) {
        List<Visit> visits = new ArrayList<>();
        if (screeningDate != null) {
            Visit screening = new Visit();
            screening.setId(1L);
            screening.setType(VisitType.SCREENING);
            screening.setDate(screeningDate);
            visits.add(screening);
        }
        if (primeVacDate != null) {
            Visit primeVacVisit = new Visit();
            primeVacVisit.setId(2L);
            primeVacVisit.setType(VisitType.PRIME_VACCINATION_DAY);
            primeVacVisit.setDateProjected(primeVacDate);
            visits.add(primeVacVisit);
        }
        subject.setVisits(visits);
    }

    private Map<VisitType, VisitScheduleOffset> createVisitScheduleOffsetMap() {
        Map<VisitType, VisitScheduleOffset> map = new HashMap<>();
        List<VisitType> types = Arrays.asList(VisitType.values());

        for (int i = 0; i < types.size(); i++) {
            VisitType type = types.get(i);
            if (type != VisitType.PRIME_VACCINATION_DAY && type != VisitType.SCREENING) {
                VisitScheduleOffset offset = new VisitScheduleOffset();
                offset.setVisitType(types.get(i));
                offset.setEarliestDateOffset(i * 2 + 1);
                offset.setLatestDateOffset(i * 2 + 10);
                offset.setTimeOffset(i * 2 + 5);
                map.put(offset.getVisitType(), offset);
            }
        }

        return map;
    }
}
