package org.motechproject.evsmbarara.util;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.util.serializer.CustomVisitListSerializer;

import java.util.List;

public abstract class SubjectVisitsMixin {

    @JsonSerialize(using = CustomVisitListSerializer.class)
    public abstract List<Visit> getVisits();
}
