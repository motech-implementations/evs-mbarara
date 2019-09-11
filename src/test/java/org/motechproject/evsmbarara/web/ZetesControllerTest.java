package org.motechproject.evsmbarara.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.evsmbarara.domain.Config;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.service.ConfigService;
import org.motechproject.evsmbarara.service.SubjectService;
import org.motechproject.evsmbarara.service.ZetesService;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;

import java.util.ArrayList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class ZetesControllerTest {

    @Mock
    private SubjectService subjectService;

    @Mock
    private ConfigService configService;

    @Mock
    private ZetesService zetesService;

    private ZetesController zetesController;

    @Before
    public void setUp() {
        initMocks(this);
        zetesController = new ZetesController();
        zetesController.setSubjectService(subjectService);
        zetesController.setConfigService(configService);
        zetesController.setZetesService(zetesService);
    }

    @Test
    public void shouldCreateOrUpdateSubjectFromZetes() {
        SubjectZetesDto subjectZetesDto = createSubjectZetesDto();

        Subject subjectTest = createSubject();

        when(subjectService.findSubjectBySubjectId(subjectZetesDto.getSubjectId())).thenReturn(subjectTest);

        zetesController.submitSubjectRequest(subjectZetesDto);
        verify(subjectService).createOrUpdateForZetes(subjectZetesDto);
    }

    @Test
    public void shouldRunZetesJob() {
        Config config = new Config();
        when(configService.getConfig()).thenReturn(config);

        zetesController.runZetesJob();

        verify(zetesService).sendUpdatedSubjects(config.getZetesUrl(), config.getZetesUsername(), config.getZetesPassword());
    }

    private SubjectZetesDto createSubjectZetesDto() {
        SubjectZetesDto subjectZetesDto = new SubjectZetesDto();
        subjectZetesDto.setPhoneNumber("123456789");
        subjectZetesDto.setName("Kasia");
        subjectZetesDto.setSubjectId("20300026");
        subjectZetesDto.setAddress("Warszawa 19");
        subjectZetesDto.setLanguage("eng");
        subjectZetesDto.setCommunity("community");
        subjectZetesDto.setSiteId("newSiteId");
        subjectZetesDto.setSiteName("siteName");
        subjectZetesDto.setChiefdom("chiefdom");
        subjectZetesDto.setSection("section");
        subjectZetesDto.setDistrict("district");
        subjectZetesDto.setAge(45);
        subjectZetesDto.setGender("male");
        return subjectZetesDto;
    }

    private Subject createSubject() {
        Subject subject = new Subject();
        subject.setSubjectId("123");
        subject.setName("name");
        subject.setGender(Gender.Male);
        subject.setPhoneNumber("123456789");
        subject.setAddress("address");
        subject.setCommunity("community");
        subject.setSiteId("siteID");
        subject.setSiteName("siteName");
        subject.setChiefdom("chiefom");
        subject.setSection("section");
        subject.setDistrict("district");
        subject.setLanguage(Language.English);
        subject.setVisits(new ArrayList<Visit>());
        subject.setAge(45);
        return subject;
    }

}
