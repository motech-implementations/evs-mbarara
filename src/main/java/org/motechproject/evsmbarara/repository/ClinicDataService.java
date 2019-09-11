package org.motechproject.evsmbarara.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.evsmbarara.domain.Clinic;

import java.util.List;

public interface ClinicDataService extends MotechDataService<Clinic> {

    @Lookup
    List<Clinic> findByLocation(
            @LookupField(name = "location", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String location);

    @Lookup
    List<Clinic> findBySiteId(
            @LookupField(name = "siteId", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String siteId);

    @Lookup
    Clinic findByExactSiteId(@LookupField(name = "siteId") String siteId);

}
