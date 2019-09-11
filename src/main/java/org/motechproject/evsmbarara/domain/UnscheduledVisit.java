package org.motechproject.evsmbarara.domain;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.evsmbarara.util.serializer.CustomDateSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomTimeSerializer;

@Entity(recordHistory = true)
public class UnscheduledVisit {

    @Field
    @Getter
    @Setter
    private Long id;

    @Field(required = true, displayName = "Participant")
    @Getter
    @Setter
    private Subject subject;

    @Field
    @Getter
    @Setter
    private Clinic clinic;

    @Field(required = true)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate date;

    @Field(required = true)
    @JsonSerialize(using = CustomTimeSerializer.class)
    @Getter
    @Setter
    private Time startTime;

    @Field(required = true)
    @JsonSerialize(using = CustomTimeSerializer.class)
    @Getter
    @Setter
    private Time endTime;

    @Field
    @Getter
    @Setter
    private String purpose;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;
}
