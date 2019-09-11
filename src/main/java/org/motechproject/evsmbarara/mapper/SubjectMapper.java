package org.motechproject.evsmbarara.mapper;

import org.joda.time.LocalDate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;

@Mapper(uses = { EnumsMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SubjectMapper {

    public static final SubjectMapper INSTANCE = Mappers.getMapper(SubjectMapper.class);

    public abstract Subject fromDto(SubjectZetesDto subjectZetesDto);

    public abstract void updateFromDto(SubjectZetesDto subjectZetesDto, @MappingTarget Subject subject);

    @AfterMapping
    protected void calculateYearOfBirth(SubjectZetesDto dto, @MappingTarget Subject subject) {
        Integer currentYear = new LocalDate().getYear();
        if (dto.getAge() != null) {
            subject.setYearOfBirth(currentYear - dto.getAge());
        }
    }

}
