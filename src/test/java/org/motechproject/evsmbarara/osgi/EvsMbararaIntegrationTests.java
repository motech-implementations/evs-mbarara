package org.motechproject.evsmbarara.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Evs Mbarara bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        EvsMbararaWebIT.class,
        EvsMbararaConfigServiceIT.class,
        LookupServiceIT.class,
        SubjectServiceIT.class
})
public class EvsMbararaIntegrationTests {
}
