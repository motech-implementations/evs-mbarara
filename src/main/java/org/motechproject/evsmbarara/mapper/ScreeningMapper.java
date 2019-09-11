package org.motechproject.evsmbarara.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.dto.ScreeningDto;

@Mapper(uses = { DateAndTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScreeningMapper {

    ScreeningMapper INSTANCE = Mappers.getMapper(ScreeningMapper.class);

    @Mappings({
            @Mapping(target = "volunteerId", source = "volunteer.id"),
            @Mapping(target = "clinicId", source = "clinic.id"),
            @Mapping(target = "name", source = "volunteer.name"),
            @Mapping(target = "contactNumber", source = "volunteer.contactNumber"),
            @Mapping(target = "additionalContact", source = "volunteer.additionalContact")
    })
    ScreeningDto toDto(Screening screening);
}
