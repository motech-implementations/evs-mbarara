package org.motechproject.evsmbarara.mapper;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class SubjectMapperTest {

    @Inject
    private SubjectMapper subjectMapper = new SubjectMapperImpl();

    @Test
    public void shouldCreateAndUpdateSubjectFromDto() {
        SubjectZetesDto dto =  createSubjectZetesDto();

        Subject subject = subjectMapper.fromDto(dto);

        checkSubjectWithDto(dto, subject);

        dto.setName("newName");
        dto.setGuardianName("newGuardName");
        dto.setAge(17);
        dto.setGender("female");
        dto.setLanguage("sus");
        dto.setSiteId("newSiteId");

        subjectMapper.updateFromDto(dto, subject);

        checkSubjectWithDto(dto, subject);
    }

    private void checkSubjectWithDto(SubjectZetesDto expected, Subject actual) {
        assertEquals(expected.getSubjectId(), actual.getSubjectId());
        assertEquals(expected.getAge(), actual.getAge());
        assertEquals(expected.getDistrict(), actual.getDistrict());
        assertEquals(expected.getSection(), actual.getSection());
        assertEquals(expected.getChiefdom(), actual.getChiefdom());
        assertEquals(expected.getSiteName(), actual.getSiteName());
        assertEquals(expected.getSiteId(), actual.getSiteId());
        assertEquals(expected.getCommunity(), actual.getCommunity());
        assertEquals(expected.getLanguage(), actual.getLanguageCode());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals(expected.getGuardianName(), actual.getGuardianName());
        assertEquals(expected.getGuardianType(), actual.getGuardianType());
        Integer expectedYearOfBirth = new LocalDate().getYear() - expected.getAge();
        assertEquals(expectedYearOfBirth, actual.getYearOfBirth());
        assertEquals(expected.getGender(), actual.getGender().getValue());
    }

    private SubjectZetesDto createSubjectZetesDto() {
        SubjectZetesDto dto = new SubjectZetesDto();
        dto.setSubjectId("subjectId");
        dto.setAge(15);
        dto.setGender("male");
        dto.setDistrict("district");
        dto.setSection("section");
        dto.setChiefdom("chiefdom");
        dto.setSiteName("siteName");
        dto.setSiteId("siteId");
        dto.setCommunity("community");
        dto.setLanguage("lma");
        dto.setAddress("address");
        dto.setName("name");
        dto.setPhoneNumber("123456678");
        dto.setGuardianName("guardName");
        dto.setGuardianType("guardType");

        return dto;
    }
}
