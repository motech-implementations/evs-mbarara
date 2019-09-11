package org.motechproject.evsmbarara.service.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.dto.PrimeVaccinationScheduleDto;
import org.motechproject.evsmbarara.exception.LimitationExceededException;
import org.motechproject.evsmbarara.helper.VisitLimitationHelper;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.LookupService;
import org.motechproject.evsmbarara.service.PrimeVaccinationScheduleService;
import org.motechproject.evsmbarara.util.QueryParamsBuilder;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("primeVaccinationScheduleService")
public class PrimeVaccinationScheduleServiceImpl implements PrimeVaccinationScheduleService {

    @Autowired
    private VisitDataService visitDataService;

    @Autowired
    private VisitLimitationHelper visitLimitationHelper;

    @Autowired
    private ClinicDataService clinicDataService;

    @Autowired
    private SubjectDataService subjectDataService;

    @Autowired
    private LookupService lookupService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Records<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords(GridSettings settings) throws IOException {
        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings, getFields(settings.getFields()));
        return lookupService.getEntities(PrimeVaccinationScheduleDto.class,
                Visit.class, settings.getLookup(), settings.getFields(), queryParams);
    }

    @Override
    public PrimeVaccinationScheduleDto createOrUpdateWithDto(PrimeVaccinationScheduleDto dto, Boolean ignoreLimitation) {
        validateDate(dto);

        Subject subject = subjectDataService.findBySubjectId(dto.getParticipantId());

        Clinic clinic = clinicDataService.findByExactSiteId(subject.getSiteId());
        if (!ignoreLimitation && clinic != null) {
            checkNumberOfPatients(dto, clinic);
        }

        Visit primeVisit = visitDataService.findById(dto.getVisitId());
        // We have an update
        if (primeVisit != null) {
            Visit screeningVisit = getScreeningVisit(primeVisit);
            if (screeningVisit == null) {
                throw new IllegalArgumentException("Cannot save, because Screening for Visit not found");
            }
            return new PrimeVaccinationScheduleDto(updateVisitWithDto(primeVisit, screeningVisit, dto));
        } else {
            List<Visit> visits = subject.getVisits();
            primeVisit = createAndCheckIfAlreadyExists(visits, dto, subject, clinic);
            visits.add(primeVisit);
            return new PrimeVaccinationScheduleDto(primeVisit);
        }
    }

    @Override
    public List<PrimeVaccinationScheduleDto> getPrimeVaccinationScheduleRecords() {
        List<PrimeVaccinationScheduleDto> primeVacDtos = new ArrayList<>();
        // Get all subjects
        List<Subject> subjects = subjectDataService.findByPrimerVaccinationDate(null);

        for (Subject subject : subjects) {
            if (subject.getVisits().isEmpty()) {
                primeVacDtos.add(new PrimeVaccinationScheduleDto(subject));
            }
        }

        return primeVacDtos;
    }

  /**
   * This method creates Screening Visit and Prime Vaccination Visit if participant doesn't have
   * visits created yet. We need to make sure if no visit already exist to avoid data duplications.
   * @param visits this list may be empty but some visits may exists - for example if two requests were
   * called at the same time, one could create visits that are not in this list.
   * @param dto contains data provided in UI
   * @param subject - participant for whom visits are created
   * @param clinic in which those visits are created
   * @return created Prime Vaccination Visit
   */
    private synchronized Visit createAndCheckIfAlreadyExists(List<Visit> visits, PrimeVaccinationScheduleDto dto,
                                                            Subject subject, Clinic clinic) {
        List<Visit> visitList = visitDataService.findByParticipantId(subject.getSubjectId());
        if (!visitList.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Visit for participant with id %s already exists.", subject.getSubjectId()));
        }

        visits.add(visitDataService.create(createScreeningVisitFromDto(dto, subject, clinic)));
        return visitDataService.create(createPrimeVacVisitFromDto(dto, subject, clinic));
    }

    private Visit createScreeningVisitFromDto(PrimeVaccinationScheduleDto dto, Subject subject, Clinic clinic) {
        Visit screeningVisit = new Visit();
        screeningVisit.setType(VisitType.SCREENING);
        screeningVisit.setSubject(subject);
        screeningVisit.setDate(dto.getActualScreeningDate());
        screeningVisit.setClinic(clinic);
        return screeningVisit;
    }

    private Visit createPrimeVacVisitFromDto(PrimeVaccinationScheduleDto dto, Subject subject, Clinic clinic) {
        Visit primeVisit = new Visit();
        primeVisit.setSubject(subject);
        primeVisit.setType(VisitType.PRIME_VACCINATION_DAY);
        primeVisit.setStartTime(dto.getStartTime());
        primeVisit.setEndTime(calculateEndTime(dto.getStartTime()));
        primeVisit.setDateProjected(dto.getDate());
        primeVisit.getSubject().setFemaleChildBearingAge(dto.getFemaleChildBearingAge());
        primeVisit.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());
        primeVisit.setClinic(clinic);
        return primeVisit;
    }

    private Visit updateVisitWithDto(Visit primeDetails, Visit screeningDetails,
                                     PrimeVaccinationScheduleDto dto) {
        primeDetails.setStartTime(dto.getStartTime());
        primeDetails.setEndTime(calculateEndTime(dto.getStartTime()));
        primeDetails.setDateProjected(dto.getDate());
        primeDetails.getSubject().setFemaleChildBearingAge(dto.getFemaleChildBearingAge());
        primeDetails.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());

        screeningDetails.setDate(dto.getActualScreeningDate());

        visitDataService.update(screeningDetails);
        return visitDataService.update(primeDetails);
    }

    private Map<String, Object> getFields(String json) throws IOException {
        if (json == null) {
            return null;
        } else {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }

    private void checkNumberOfPatients(PrimeVaccinationScheduleDto dto, Clinic clinic) { //NO CHECKSTYLE CyclomaticComplexity
        List<Visit> visits = visitDataService.findByPlannedDateClinicIdAndVisitType(dto.getDate(),
                clinic.getId(), VisitType.PRIME_VACCINATION_DAY);

        visitLimitationHelper.checkCapacityForVisit(dto.getDate(), clinic, dto.getVisitId());
        if (visits != null) {
            Integer numberOfRooms = clinic.getNumberOfRooms();
            int maxVisits = clinic.getMaxPrimeVisits();
            int patients = 0;

            for (Visit visit : visits) {
                if (visit.getId().equals(dto.getVisitId())) {
                    maxVisits++;
                } else {
                    Time startTime = dto.getStartTime();
                    Time endTime = calculateEndTime(startTime);

                    if (startTime.isBefore(visit.getStartTime())) {
                        if (visit.getStartTime().isBefore(endTime)) {
                            patients++;
                        }
                    } else {
                        if (startTime.isBefore(visit.getEndTime())) {
                            patients++;
                        }
                    }
                }
            }

            if (visits.size() >= maxVisits) {
                throw new LimitationExceededException("The booking limit for this type of visit has been reached");
            }
            if (numberOfRooms != null && patients >= numberOfRooms) {
                throw new LimitationExceededException("Too many visits at the same time");
            }
        }
    }

    private void validateDate(PrimeVaccinationScheduleDto dto) {
        if (dto.getActualScreeningDate() == null) {
            throw new IllegalArgumentException("Screening Date cannot be empty");
        }
        if (dto.getDate() == null) {
            throw new IllegalArgumentException("Prime Vaccination Planned Date cannot be empty");
        }

        if (!dto.getIgnoreDateLimitation()) {
            if (dto.getDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("The date can not be in past");
            }

            LocalDate actualScreeningDate = dto.getActualScreeningDate();

            LocalDate earliestDate = dto.getFemaleChildBearingAge() != null && dto.getFemaleChildBearingAge()
                    ? actualScreeningDate.plusDays(EvsMbararaConstants.EARLIEST_DATE_IF_FEMALE_CHILD_BEARING_AGE)
                    : actualScreeningDate;
            LocalDate latestDate = actualScreeningDate.plusDays(EvsMbararaConstants.LATEST_DATE);

            if (dto.getDate().isBefore(earliestDate) || dto.getDate().isAfter(latestDate)) {
                throw new IllegalArgumentException(String.format("The date should be between %s and %s but is %s",
                        earliestDate, latestDate, dto.getDate()));
            }
        }
    }

    private Visit getScreeningVisit(Visit visit) {
        if (visit != null) {
            for (Visit v : visit.getSubject().getVisits()) {
                if (VisitType.SCREENING.equals(v.getType())) {
                    return v;
                }
            }
        }
        return null;
    }

    private Time calculateEndTime(Time startTime) {
        int endTimeHour = (startTime.getHour() + EvsMbararaConstants.TIME_OF_THE_VISIT) % EvsMbararaConstants.MAX_TIME_HOUR;
        return new Time(endTimeHour, startTime.getMinute());
    }
}
