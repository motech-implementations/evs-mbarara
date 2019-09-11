package org.motechproject.evsmbarara.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.util.serializer.CustomBooleanDeserializer;
import org.motechproject.evsmbarara.util.serializer.CustomBooleanSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomDateDeserializer;
import org.motechproject.evsmbarara.util.serializer.CustomDateSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomTimeSerializer;

@NoArgsConstructor
public class PrimeVaccinationScheduleDto {

    @Getter
    @Setter
    private Long visitId;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private String participantId;

    @Getter
    @Setter
    private String participantName;

    @Getter
    @Setter
    private Gender participantGender;

    @JsonSerialize(using = CustomBooleanSerializer.class)
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    @Getter
    @Setter
    private Boolean femaleChildBearingAge;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate actualScreeningDate;

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
    private Boolean ignoreDateLimitation;

    public PrimeVaccinationScheduleDto(Visit visit) {

        for (Visit v : visit.getSubject().getVisits()) {
            if (VisitType.SCREENING.equals(v.getType())) {
                setActualScreeningDate(v.getDate());
                break;
            }
        }

        setStartTime(visit.getStartTime());
        setParticipantId(visit.getSubject().getSubjectId());
        setParticipantName(visit.getSubject().getName());
        setDate(visit.getDateProjected());
        if (visit.getSubject().getGender() == null || visit.getSubject().getGender().equals(Gender.Female)) {
            setFemaleChildBearingAge(visit.getSubject().getFemaleChildBearingAge());
        } else {
            setFemaleChildBearingAge(false);
        }
        setVisitId(visit.getId());
        setParticipantGender(visit.getSubject().getGender());
        if (visit.getIgnoreDateLimitation() != null) {
            setIgnoreDateLimitation(visit.getIgnoreDateLimitation());
        } else {
            setIgnoreDateLimitation(false);
        }
        if (visit.getClinic() != null) {
            setLocation(visit.getClinic().getLocation());
        }
    }

    public PrimeVaccinationScheduleDto(Subject subject) {
        setParticipantId(subject.getSubjectId());
        setParticipantName(subject.getName());
        if (subject.getGender() == null || subject.getGender().equals(Gender.Female)) {
            setFemaleChildBearingAge(subject.getFemaleChildBearingAge());
        } else {
            setFemaleChildBearingAge(false);
        }
        setParticipantGender(subject.getGender());
        setIgnoreDateLimitation(false);
    }
}
