package org.motechproject.evsmbarara.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;
import org.motechproject.evsmbarara.dto.UnscheduledVisitDto;
import org.motechproject.evsmbarara.helper.VisitLimitationHelper;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.service.impl.UnscheduledVisitServiceImpl;
import org.motechproject.evsmbarara.web.domain.GridSettings;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UnscheduledVisitServiceTest {

    @InjectMocks
    private UnscheduledVisitService unscheduledVisitService = new UnscheduledVisitServiceImpl();

    @Mock
    private LookupService lookupService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Mock
    private SubjectDataService subjectDataService;

    @Mock
    private VisitLimitationHelper visitLimitationHelper;

    @Mock
    private ClinicDataService clinicDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldGetUnscheduletVisitRecords() throws IOException {
        unscheduledVisitService.getUnscheduledVisitsRecords(new GridSettings());

        verify(lookupService).getEntities(any(UnscheduledVisitDto.class.getClass()), any(UnscheduledVisit.class.getClass()),
                any(String.class), any(String.class), any(QueryParams.class));
    }

    @Test
    public void shouldAddUnscheduledVisit() {
        String subjectId = "subjectId";
        UnscheduledVisitDto unscheduledVisitDto = new UnscheduledVisitDto();
        unscheduledVisitDto.setDate(new LocalDate(2017, 4, 15));
        unscheduledVisitDto.setStartTime(new Time(1, 1));
        unscheduledVisitDto.setPurpose("purpose");
        unscheduledVisitDto.setParticipantId(subjectId);

        Subject subject = createSubject(subjectId);
        Clinic clinic = createClinic(subject);
        UnscheduledVisit unscheduledVisit = createUnscheduledVisit(subject, clinic, new LocalDate(2017, 4, 15), new Time(1, 1));

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(clinicDataService.findByExactSiteId(subject.getSiteId())).thenReturn(clinic);
        when(unscheduledVisitDataService.create(any(UnscheduledVisit.class))).thenReturn(unscheduledVisit);

        UnscheduledVisitDto resultDto = unscheduledVisitService.addOrUpdate(unscheduledVisitDto, false);

        verify(visitLimitationHelper).checkCapacityForUnscheduleVisit(unscheduledVisitDto.getDate(),
                clinic, null);
        verify(unscheduledVisitDataService).create(any(UnscheduledVisit.class));

        assertEquals(subjectId, resultDto.getParticipantId());
        assertEquals(clinic.getLocation(), resultDto.getClinicName());
        assertEquals(new LocalDate(2017, 4, 15), resultDto.getDate());
        assertEquals(new Time(1, 1), resultDto.getStartTime());
        assertEquals("purpose", resultDto.getPurpose());
    }

    @Test
    public void shouldUpdateUnscheduledVisit() {
        String subjectId = "subjectId";
        UnscheduledVisitDto unscheduledVisitDto = new UnscheduledVisitDto();
        unscheduledVisitDto.setDate(new LocalDate(2017, 4, 16));
        unscheduledVisitDto.setStartTime(new Time(2, 1));
        unscheduledVisitDto.setPurpose("purpose");
        unscheduledVisitDto.setParticipantId(subjectId);
        unscheduledVisitDto.setId("1");

        Subject subject = createSubject(subjectId);
        Clinic clinic = createClinic(subject);
        UnscheduledVisit unscheduledVisitInDB = createUnscheduledVisit(subject, clinic, new LocalDate(2017, 4, 15), new Time(1, 1));

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(clinicDataService.findByExactSiteId(subject.getSiteId())).thenReturn(clinic);
        when(unscheduledVisitDataService.create(any(UnscheduledVisit.class))).thenReturn(
                createUnscheduledVisit(subject, clinic, new LocalDate(2017, 4, 16), new Time(2, 1)));
        when(unscheduledVisitDataService.findById(1L)).thenReturn(unscheduledVisitInDB);

        UnscheduledVisitDto resultDto = unscheduledVisitService.addOrUpdate(unscheduledVisitDto, false);

        verify(visitLimitationHelper).checkCapacityForUnscheduleVisit(unscheduledVisitDto.getDate(),
                clinic, Long.parseLong(unscheduledVisitDto.getId()));
        verify(unscheduledVisitDataService).create(any(UnscheduledVisit.class));

        assertEquals(subjectId, resultDto.getParticipantId());
        assertEquals(clinic.getLocation(), resultDto.getClinicName());
        assertEquals(new LocalDate(2017, 4, 16), resultDto.getDate());
        assertEquals(new Time(2, 1), resultDto.getStartTime());
        assertEquals("purpose", resultDto.getPurpose());
    }

    private Subject createSubject(String subjectId) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        subject.setSiteId("siteId");
        subject.setSiteName("siteName");
        return subject;
    }

    private Clinic createClinic(Subject subject) {
        Clinic clinic = new Clinic();
        clinic.setId(1L);
        clinic.setLocation(subject.getSiteName());
        clinic.setSiteId(subject.getSiteId());
        return clinic;
    }

    private UnscheduledVisit createUnscheduledVisit(Subject subject, Clinic clinic, LocalDate date, Time startTime) {
        UnscheduledVisit unscheduledVisit = new UnscheduledVisit();
        unscheduledVisit.setId(1L);
        unscheduledVisit.setClinic(clinic);
        unscheduledVisit.setDate(date);
        unscheduledVisit.setStartTime(startTime);
        unscheduledVisit.setPurpose("purpose");
        unscheduledVisit.setSubject(subject);
        return unscheduledVisit;
    }
}
