package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.dto.ScreeningDto;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;

import java.io.IOException;

public interface ScreeningService {

    Records<Screening> getScreenings(GridSettings gridSettings) throws IOException;

    Screening addOrUpdate(ScreeningDto screeningDto, Boolean ignoreLimitation);

    ScreeningDto getScreeningById(Long id);

    void cancelScreening(Long id);

    void activateScreening(Long id, Boolean ignoreLimitation);

    void completeScreening(Long id);
}
