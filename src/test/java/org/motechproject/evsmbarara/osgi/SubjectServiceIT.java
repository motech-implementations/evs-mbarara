package org.motechproject.evsmbarara.osgi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.SubjectService;
import org.motechproject.evsmbarara.utils.SubjectUtil;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SubjectServiceIT extends BasePaxIT {

    private static final String NEW_ADDRESS = "newAddress";
    private static final String NEW_LANGUAGE = "lma";
    private static final String NEW_PHONE_NUMBER = "600700800";
    private static final String NEW_SITE_NAME = "newSiteName";
    private static final String NEW_SITE_ID = "newSiteId";

    @Inject
    private SubjectService subjectService;

    @Inject
    private SubjectDataService subjectDataService;

    @Inject
    private VisitDataService visitDataService;

    private Subject firstSubject;

    private Subject secondSubject;

    @Before
    public void cleanBefore() {
        visitDataService.deleteAll();
        subjectDataService.deleteAll();
        resetSubjects();
    }

    @After
    public void cleanAfter() {
        visitDataService.deleteAll();
        subjectDataService.deleteAll();
    }

    private void resetSubjects() {
        firstSubject = SubjectUtil.createSubject("1000000161", "Michal", "729402018364", Language.English);

        secondSubject = SubjectUtil.createSubject("1000000162", "Rafal", "44443333222", Language.Susu);
    }

    @Test
    public void shouldCreateOrUpdateForZetes() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        subjectService.create(firstSubject);
        List<Subject> subjects = subjectDataService.retrieveAll();
        assertEquals(1, subjects.size());

        checkZetesFields(firstSubject, subjects.get(0));

        SubjectZetesDto firstSubjectZetesDto = createDtoFromSubject(firstSubject);
        firstSubjectZetesDto.setAddress(NEW_ADDRESS);
        firstSubjectZetesDto.setLanguage(NEW_LANGUAGE);
        firstSubjectZetesDto.setPhoneNumber(NEW_PHONE_NUMBER);
        firstSubjectZetesDto.setSiteName(NEW_SITE_NAME);
        firstSubjectZetesDto.setSiteId(NEW_SITE_ID);

        subjectService.createOrUpdateForZetes(firstSubjectZetesDto); // should update Subject
        subjects = subjectDataService.retrieveAll();
        assertEquals(1, subjects.size());

        updateFirstSubject();
        checkZetesFields(firstSubject, subjects.get(0));

        SubjectZetesDto secondSubjectZetesDto = createDtoFromSubject(secondSubject);

        subjectService.createOrUpdateForZetes(secondSubjectZetesDto); // should create new Subject
        subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        Subject subjectFromDataService = subjectDataService.findBySubjectId("1000000162");
        assertNotNull(subjectFromDataService);

        checkZetesFields(secondSubject, subjectFromDataService);

        secondSubject.setId(subjectFromDataService.getId());
        secondSubject.setName("Jedrzej");
        secondSubject.setCommunity("newCommunity");

        subjectDataService.update(secondSubject);
        subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        subjectFromDataService = subjectDataService.findBySubjectId("1000000162");
        assertNotNull(subjectFromDataService);

        checkZetesFields(secondSubject, subjectFromDataService);
    }

    @Test
    public void shouldFindSubjectBySubjectId() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        subjectService.create(firstSubject);
        subjectService.create(secondSubject);

        List<Subject> subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        Subject subject = subjectService.findSubjectBySubjectId("1000000161");
        assertNotNull(subject);
        assertEquals("1000000161", subject.getSubjectId());
    }

    @Test
    public void shouldFindModifiedSubjects() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        subjectService.create(firstSubject);
        subjectService.create(secondSubject);

        List<Subject> subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        firstSubject.setChanged(true);

        subjectService.update(firstSubject);

        subjects = subjectDataService.retrieveAll();
        assertEquals(2, subjects.size());

        subjects = subjectService.findModifiedSubjects();
        assertEquals(1, subjects.size());

        assertEquals("1000000161", subjects.get(0).getSubjectId());
    }

    @Test
    public void shouldFindSubjectById() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        subjectService.create(firstSubject);
        subjectService.create(secondSubject);

        Subject subject = subjectService.findSubjectBySubjectId(firstSubject.getSubjectId());

        assertEquals("Michal", subject.getName());
    }

    private void checkZetesFields(Subject expected, Subject actual) {
        assertEquals(expected.getSubjectId(), actual.getSubjectId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getLanguageCode(), actual.getLanguageCode());
        assertEquals(expected.getCommunity(), actual.getCommunity());
        assertEquals(expected.getSiteId(), actual.getSiteId());
        assertEquals(expected.getSiteName(), actual.getSiteName());
        assertEquals(expected.getChiefdom(), actual.getChiefdom());
        assertEquals(expected.getSection(), actual.getSection());
        assertEquals(expected.getDistrict(), actual.getDistrict());
        assertEquals(expected.getAge(), actual.getAge());
        assertEquals(expected.getGender(), actual.getGender());
    }

    private SubjectZetesDto createDtoFromSubject(Subject subject) {
        SubjectZetesDto dto = new SubjectZetesDto();
        dto.setSubjectId(subject.getSubjectId());
        dto.setName(subject.getName());
        dto.setAddress(subject.getAddress());
        dto.setLanguage(subject.getLanguageCode());
        dto.setCommunity(subject.getCommunity());
        dto.setSiteId(subject.getSiteId());
        dto.setSiteName(subject.getSiteName());
        dto.setChiefdom(subject.getChiefdom());
        dto.setSection(subject.getSection());
        dto.setDistrict(subject.getDistrict());
        dto.setAge(subject.getAge());
        dto.setGender(subject.getGender().getValue());

        return dto;
    }

    private void updateFirstSubject() {
        firstSubject.setAddress(NEW_ADDRESS);
        firstSubject.setSiteId(NEW_SITE_ID);
        firstSubject.setSiteName(NEW_SITE_NAME);
        firstSubject.setLanguage(Language.getByCode(NEW_LANGUAGE));
        firstSubject.setPhoneNumber(NEW_PHONE_NUMBER);
    }
}
