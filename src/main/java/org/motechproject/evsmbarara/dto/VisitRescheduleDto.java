package org.motechproject.evsmbarara.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.util.serializer.CustomDateDeserializer;
import org.motechproject.evsmbarara.util.serializer.CustomDateSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomVisitTypeDeserializer;
import org.motechproject.evsmbarara.util.serializer.CustomVisitTypeSerializer;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class VisitRescheduleDto {

    @Getter
    @Setter
    private String participantId;

    @JsonSerialize(using = CustomVisitTypeSerializer.class)
    @JsonDeserialize(using = CustomVisitTypeDeserializer.class)
    @Getter
    @Setter
    private VisitType visitType;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate actualDate;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate plannedDate;

    @Getter
    @Setter
    private Long visitId;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate earliestDate;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate latestDate;

    @Getter
    @Setter
    private Boolean ignoreDateLimitation;

    @Getter
    @Setter
    private Boolean boosterRelated;

    @Getter
    @Setter
    private Boolean notVaccinated;

    public VisitRescheduleDto(Visit visit) {
        setParticipantId(visit.getSubject().getSubjectId());
        setVisitType(visit.getType());
        setActualDate(visit.getDate());
        setPlannedDate(visit.getDateProjected());
        setVisitId(visit.getId());
        if (visit.getIgnoreDateLimitation() != null) {
            setIgnoreDateLimitation(visit.getIgnoreDateLimitation());
        } else {
            setIgnoreDateLimitation(false);
        }
    }

    public VisitRescheduleDto(Visit visit, Range<LocalDate> dateRange,
                              Boolean boosterRelated, Boolean notVaccinated) {
        this(visit);
        this.boosterRelated = boosterRelated;
        this.notVaccinated = notVaccinated;
        calculateEarliestAndLatestDate(dateRange);
    }

    private void calculateEarliestAndLatestDate(Range<LocalDate> dateRange) {
        if (dateRange != null) {
            earliestDate = dateRange.getMin();
            latestDate = dateRange.getMax();
        }
    }
}
