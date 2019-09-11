package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.dto.PrimeVaccinationScheduleDto;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;

import java.io.IOException;
import java.util.List;

public interface PrimeVaccinationScheduleService {

    Records<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords(
            GridSettings settings) throws IOException;

    PrimeVaccinationScheduleDto createOrUpdateWithDto(PrimeVaccinationScheduleDto visitDto,
                                                      Boolean ignoreLimitation);

    List<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords();
}
