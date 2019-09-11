package org.motechproject.evsmbarara.domain;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.EnumDisplayName;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;
import org.motechproject.evsmbarara.util.serializer.CustomDateSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomScreeningStatusSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomTimeSerializer;

@Entity(recordHistory = true)
public class Screening {

    public static final String DATE_PROPERTY_NAME = "date";

    @Field
    @Getter
    @Setter
    private Long id;

    @Field
    @Getter
    @Setter
    private Clinic clinic;

    @Field(required = true)
    @Getter
    @Setter
    private Volunteer volunteer;

    @Field(required = true)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate date;

    @Field
    @JsonSerialize(using = CustomTimeSerializer.class)
    @Getter
    @Setter
    private Time startTime;

    @Field
    @JsonSerialize(using = CustomTimeSerializer.class)
    @Getter
    @Setter
    private Time endTime;

    @Field
    @JsonSerialize(using = CustomScreeningStatusSerializer.class)
    @EnumDisplayName(enumField = "value")
    @Getter
    @Setter
    private ScreeningStatus status;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    public Screening() {
        status = ScreeningStatus.ACTIVE;
    }
}
