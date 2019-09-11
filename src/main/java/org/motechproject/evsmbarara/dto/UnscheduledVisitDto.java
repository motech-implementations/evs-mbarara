package org.motechproject.evsmbarara.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.evsmbarara.domain.UnscheduledVisit;
import org.motechproject.evsmbarara.util.serializer.CustomDateDeserializer;
import org.motechproject.evsmbarara.util.serializer.CustomDateSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomTimeSerializer;

@NoArgsConstructor
public class UnscheduledVisitDto {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String participantId;

    @Getter
    @Setter
    private String clinicName;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate date;

    @JsonSerialize(using = CustomTimeSerializer.class)
    @Getter
    @Setter
    private Time startTime;

    @Getter
    @Setter
    private String purpose;

    public UnscheduledVisitDto(UnscheduledVisit unscheduledVisit) {
        setId(unscheduledVisit.getId().toString());
        setParticipantId(unscheduledVisit.getSubject().getSubjectId());
        if (unscheduledVisit.getClinic() != null) {
            setClinicName(unscheduledVisit.getClinic().getLocation());
        }
        setDate(unscheduledVisit.getDate());
        setStartTime(unscheduledVisit.getStartTime());
        setPurpose(unscheduledVisit.getPurpose());
    }
}
