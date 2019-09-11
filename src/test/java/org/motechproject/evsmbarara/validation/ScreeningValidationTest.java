package org.motechproject.evsmbarara.validation;

import org.junit.Test;
import org.motechproject.evsmbarara.dto.ScreeningDto;

public class ScreeningValidationTest {

    @Test
    public void testValidationForAdd() {
        ScreeningDto screeningDto = createScreeningDto();

        ScreeningValidator.validateForAdd(screeningDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenVolunteerIdNotEmpty() {
        ScreeningDto screeningDto = createScreeningDto();
        screeningDto.setVolunteerId("id");

        ScreeningValidator.validateForAdd(screeningDto);
    }

    @Test
    public void testValidationForUpdate() {
        ScreeningDto screeningDto = createScreeningDto();
        screeningDto.setVolunteerId("id");

        ScreeningValidator.validateForUpdate(screeningDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldThrowIllegalArgumentExceptionWhenVolunteerIdIsEmpty() {
        ScreeningDto screeningDto = createScreeningDto();

        ScreeningValidator.validateForUpdate(screeningDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldThrowIllegalArgumentExceptionWhenClinicIdIsEmpty() {
        ScreeningDto screeningDto = new ScreeningDto();

        ScreeningValidator.validateForAdd(screeningDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldThrowIllegalArgumentExceptionWhenDateIsEmpty() {
        ScreeningDto screeningDto = new ScreeningDto();
        screeningDto.setDate("2017-01-01");

        ScreeningValidator.validateForAdd(screeningDto);
    }

    private ScreeningDto createScreeningDto() {
        ScreeningDto screeningDto = new ScreeningDto();
        screeningDto.setDate("2017-01-01");
        screeningDto.setClinicId("clinic");

        return screeningDto;
    }
}
