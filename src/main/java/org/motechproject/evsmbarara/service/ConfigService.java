package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.domain.Config;

public interface ConfigService {

    Config getConfig();

    void updateConfig(Config config);
}
