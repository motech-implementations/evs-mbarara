package org.motechproject.evsmbarara.repository;

import org.motechproject.evsmbarara.domain.VotoLanguage;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;

/**
 * Interface for repository that persists simple records and allows CRUD.
 * MotechDataService base class will provide the implementation of this class as well
 * as methods for adding, deleting, saving and finding all instances.  In this class we
 * define and custom lookups we may need.
 */
public interface VotoLanguageDataService extends MotechDataService<VotoLanguage> {

    @Lookup
    VotoLanguage findByLanguage(@LookupField(name = "language") Language language);
}
