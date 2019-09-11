package org.motechproject.evsmbarara.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.impl.SubjectServiceImpl;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SubjectServiceTest {

    @InjectMocks
    private SubjectService subjectService = new SubjectServiceImpl();

    @Mock
    private SubjectDataService subjectDataService;

    @Mock
    private ClinicDataService clinicDataService;

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldCreateSubjectForZetes() {
        String subjectId = "subjectId";
        SubjectZetesDto subjectZetesDto = createSubjectZetesDto(subjectId, "newSiteId");

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(null);

        subjectService.createOrUpdateForZetes(subjectZetesDto);

        verify(subjectDataService).create(any(Subject.class));
    }

    @Test
    public void shouldUpdateSubjectForZetes() {
        String subjectId = "subjectId";
        SubjectZetesDto subjectZetesDto = createSubjectZetesDto(subjectId, "siteId");
        Subject subject = createSubject(subjectId);

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);

        subjectService.createOrUpdateForZetes(subjectZetesDto);

        verify(subjectDataService).update(subject);
    }

    @Test
    public void shouldUpdateSubjectForZetesAndSubjectSiteId() {
        String subjectId = "subjectId";
        SubjectZetesDto subjectZetesDto = createSubjectZetesDto(subjectId, "newSiteId");
        Subject subject = createSubject(subjectId);
        List<Visit> visits = new ArrayList<>(Arrays.asList(new Visit(), new Visit(), new Visit()));
        List<UnscheduledVisit> unscheduledVisits = new ArrayList<>(Collections.singletonList(new UnscheduledVisit()));

        when(subjectDataService.findBySubjectId(subjectId)).thenReturn(subject);
        when(clinicDataService.findByExactSiteId(subjectZetesDto.getSiteId())).thenReturn(createClinic());
        when(visitDataService.findBySubjectId(subjectZetesDto.getSubjectId())).thenReturn(visits);
        when(unscheduledVisitDataService.findByParticipantId(subjectZetesDto.getSubjectId())).thenReturn(unscheduledVisits);

        subjectService.createOrUpdateForZetes(subjectZetesDto);

        verify(visitDataService, times(visits.size())).update(any(Visit.class));
        verify(unscheduledVisitDataService, times(unscheduledVisits.size())).update(any(UnscheduledVisit.class));

        verify(subjectDataService).update(subject);
    }

    private SubjectZetesDto createSubjectZetesDto(String subjectId, String siteId) {
        SubjectZetesDto dto = new SubjectZetesDto();
        dto.setSubjectId(subjectId);
        dto.setPhoneNumber("123456789");
        dto.setCommunity("community");
        dto.setSiteId(siteId);
        dto.setSiteName("siteName");
        dto.setChiefdom("chiefdom");
        dto.setSection("section");
        dto.setDistrict("district");
        dto.setName("Kasia");
        dto.setAddress("Warszawa 19");
        dto.setLanguage("eng");
        dto.setAge(18);
        dto.setGender("male");
        return dto;
    }

    private Subject createSubject(String subjectId) {
        Subject newSubject = new Subject();
        newSubject.setSubjectId(subjectId);
        newSubject.setName("name");
        newSubject.setGender(Gender.Male);
        newSubject.setPhoneNumber("123456789");
        newSubject.setAddress("address");
        newSubject.setCommunity("community");
        newSubject.setSiteId("siteId");
        newSubject.setSiteName("siteName");
        newSubject.setChiefdom("chiefom");
        newSubject.setSection("section");
        newSubject.setDistrict("district");
        newSubject.setLanguage(Language.English);
        newSubject.setVisits(new ArrayList<Visit>());
        return newSubject;
    }

    private Clinic createClinic() {
        Clinic clinic = new Clinic();
        clinic.setId(1L);
        clinic.setSiteId("siteID");
        clinic.setLocation("location");
        return clinic;
    }

}
