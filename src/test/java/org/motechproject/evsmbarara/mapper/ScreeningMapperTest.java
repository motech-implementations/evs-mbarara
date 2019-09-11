package org.motechproject.evsmbarara.mapper;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.motechproject.commons.date.model.Time;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.domain.Volunteer;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;
import org.motechproject.evsmbarara.dto.ScreeningDto;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class ScreeningMapperTest {

    @Inject
    private ScreeningMapper screeningMapper = new ScreeningMapperImpl();

    @Test
    public void shouldCreateScreeningDto() {
        Screening screening = createScreening();

        ScreeningDto dto = screeningMapper.toDto(screening);

        assertEquals(screening.getId().toString(), dto.getId());
        assertEquals(screening.getDate(), LocalDate.parse(dto.getDate(), DateTimeFormat.forPattern(
            EvsMbararaConstants.SIMPLE_DATE_FORMAT)));
        assertEquals(screening.getStartTime(), Time.valueOf(dto.getStartTime()));

        assertEquals(screening.getClinic().getId().toString(), dto.getClinicId());
        assertEquals(screening.getClinic().getLocation(), dto.getClinicLocation());

        assertEquals(screening.getVolunteer().getContactNumber(), dto.getContactNumber());
        assertEquals(screening.getVolunteer().getId().toString(), dto.getVolunteerId());
        assertEquals(screening.getVolunteer().getName(), dto.getName());
    }

    private Screening createScreening() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setDate(new LocalDate(2017, 1, 1));
        screening.setStartTime(new Time(1, 0));
        screening.setEndTime(new Time(2, 0));
        screening.setStatus(ScreeningStatus.ACTIVE);
        screening.setOwner("owner");

        Clinic clinic = new Clinic();
        clinic.setId(2L);
        screening.setClinic(clinic);

        Volunteer volunteer = new Volunteer();
        volunteer.setId(3L);
        volunteer.setName("name");
        volunteer.setContactNumber("1234");
        screening.setVolunteer(volunteer);

        return screening;
    }
}
