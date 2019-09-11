package org.motechproject.evsmbarara.service.impl;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.VisitScheduleOffset;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.dto.VisitRescheduleDto;
import org.motechproject.evsmbarara.exception.LimitationExceededException;
import org.motechproject.evsmbarara.helper.VisitLimitationHelper;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.ConfigService;
import org.motechproject.evsmbarara.service.LookupService;
import org.motechproject.evsmbarara.service.VisitRescheduleService;
import org.motechproject.evsmbarara.service.VisitScheduleOffsetService;
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

@Service("visitRescheduleService")
public class VisitRescheduleServiceImpl implements VisitRescheduleService {

    @Autowired
    private LookupService lookupService;

    @Autowired
    private VisitDataService visitDataService;

    @Autowired
    private VisitScheduleOffsetService visitScheduleOffsetService;

    @Autowired
    private VisitLimitationHelper visitLimitationHelper;

    @Autowired
    private SubjectDataService subjectDataService;

    @Autowired
    private ConfigService configService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Records<VisitRescheduleDto> getVisitsRecords(GridSettings settings) throws IOException {
        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings, getFields(settings.getFields()));
        Records<Visit> detailsRecords = lookupService.getEntities(Visit.class, settings.getLookup(), settings.getFields(), queryParams);

        Map<VisitType, VisitScheduleOffset> offsetMap = visitScheduleOffsetService.getAllAsMap();
        List<String> boosterRelatedVisits = configService.getConfig().getBoosterRelatedVisits();

        List<VisitRescheduleDto> dtos = new ArrayList<>();

        for (Visit visit : detailsRecords.getRows()) {

            Boolean boosterRelated = isBoosterRelated(visit.getType(), boosterRelatedVisits);
            LocalDate vaccinationDate = getVaccinationDate(visit, boosterRelated);

            Boolean notVaccinated = true;
            Range<LocalDate> dateRange = null;

            if (vaccinationDate != null) {
                dateRange = calculateEarliestAndLatestDate(visit.getType(), offsetMap, vaccinationDate);
                notVaccinated = false;
            }

            dtos.add(new VisitRescheduleDto(visit, dateRange, boosterRelated, notVaccinated));
        }

        return new Records<>(detailsRecords.getPage(), detailsRecords.getTotal(), detailsRecords.getRecords(), dtos);
    }

    @Override
    public VisitRescheduleDto saveVisitReschedule(VisitRescheduleDto visitRescheduleDto, Boolean ignoreLimitation) {
        Visit visit = visitDataService.findById(visitRescheduleDto.getVisitId());

        if (visit == null) {
            throw new IllegalArgumentException("Cannot reschedule, because details for Visit not found");
        }

        Clinic clinic = visit.getClinic();

        validateDates(visitRescheduleDto, visit);

        if (clinic != null && !ignoreLimitation && visitRescheduleDto.getActualDate() == null) {
            checkNumberOfPatients(visitRescheduleDto, clinic);
        }

        return new VisitRescheduleDto(updateVisitDetailsWithDto(visit, visitRescheduleDto));
    }

    private void checkNumberOfPatients(VisitRescheduleDto dto, Clinic clinic) { //NO CHECKSTYLE CyclomaticComplexity

        List<Visit> visits = visitDataService
                .findByClinicIdVisitPlannedDateAndType(clinic.getId(), dto.getPlannedDate(), dto.getVisitType());

        visitLimitationHelper.checkCapacityForVisit(dto.getPlannedDate(), clinic, dto.getVisitId());

        if (visits != null && !visits.isEmpty()) {
            Integer numberOfRooms = clinic.getNumberOfRooms();
            int maxVisits = visitLimitationHelper.getMaxVisitCountForVisitType(dto.getVisitType(), clinic);
            int patients = 0;

            Time startTime = dto.getStartTime();
            Time endTime = null;

            if (startTime != null) {
                endTime = calculateEndTime(startTime);
            }

            for (Visit visit : visits) {
                if (visit.getId().equals(dto.getVisitId())) {
                    maxVisits++;
                } else if (startTime != null && visit.getStartTime() != null) {
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

    private void validateDates(VisitRescheduleDto dto, Visit visit) {
        if (dto.getActualDate() != null) {
            validateActualDate(dto);
        } else {
            validatePlannedDate(dto, visit);
        }
    }

    private void validateActualDate(VisitRescheduleDto dto) {
        if (dto.getActualDate().isAfter(new LocalDate())) {
            throw new IllegalArgumentException("Actual Date cannot be in the future.");
        }
    }

    private void validatePlannedDate(VisitRescheduleDto dto, Visit visit) {
        LocalDate plannedDate = dto.getPlannedDate();
        //If plannedDate isn't actually updated then can be in past
        if (plannedDate.isBefore(LocalDate.now()) && !plannedDate.equals(visit.getDateProjected())) {
            throw new IllegalArgumentException("Planned Date cannot be in the past.");
        }

        if (!dto.getIgnoreDateLimitation()) {
            Map<VisitType, VisitScheduleOffset> visitTypeOffsetMap = visitScheduleOffsetService.getAllAsMap();
            List<String> boosterRelatedVisits = configService.getConfig().getBoosterRelatedVisits();

            Range<LocalDate> dateRange = calculateEarliestAndLatestDate(visit, visitTypeOffsetMap, boosterRelatedVisits);

            if (dateRange == null) {
                throw new IllegalArgumentException("Cannot calculate Earliest and Latest Date");
            }

            LocalDate earliestDate = dateRange.getMin();
            LocalDate latestDate = dateRange.getMax();

            if (plannedDate.isBefore(earliestDate) || plannedDate.isAfter(latestDate)) {
                throw new IllegalArgumentException(String.format("The date should be between %s and %s but is %s",
                        earliestDate, latestDate, plannedDate));
            }
        }
    }

    private Visit updateVisitDetailsWithDto(Visit visit, VisitRescheduleDto dto) {
        Time startTime = dto.getStartTime();
        if (startTime != null) {
            visit.setStartTime(startTime);
            visit.setEndTime(calculateEndTime(startTime));
        }
        visit.setIgnoreDateLimitation(dto.getIgnoreDateLimitation());
        visit.setDateProjected(dto.getPlannedDate());
        visit.setDate(dto.getActualDate());
        if (VisitType.BOOST_VACCINATION_DAY.equals(dto.getVisitType())) {
            Subject subject = visit.getSubject();
            subject.setBoosterVaccinationDate(dto.getActualDate());
            subjectDataService.update(subject);
        }

        return visitDataService.update(visit);
    }

    private Map<String, Object> getFields(String json) throws IOException {
        if (json == null) {
            return null;
        } else {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap>() {
            });  //NO CHECKSTYLE WhitespaceAround
        }
    }

    private Time calculateEndTime(Time startTime) {
        int endTimeHour = (startTime.getHour() + EvsMbararaConstants.TIME_OF_THE_VISIT) % EvsMbararaConstants.MAX_TIME_HOUR;
        return new Time(endTimeHour, startTime.getMinute());
    }

    private Range<LocalDate> calculateEarliestAndLatestDate(Visit visit, Map<VisitType, VisitScheduleOffset> visitTypeOffset,
                                                            List<String> boosterRelatedVisits) {
        Boolean boosterRelated = isBoosterRelated(visit.getType(), boosterRelatedVisits);
        LocalDate vaccinationDate = getVaccinationDate(visit, boosterRelated);

        if (vaccinationDate == null) {
            return null;
        }

        return calculateEarliestAndLatestDate(visit.getType(), visitTypeOffset, vaccinationDate);
    }

    private Range<LocalDate> calculateEarliestAndLatestDate(VisitType visitType, Map<VisitType, VisitScheduleOffset> visitTypeOffset,
                                                            LocalDate vaccinationDate) {
        if (visitTypeOffset == null) {
            return null;
        }

        VisitScheduleOffset offset = visitTypeOffset.get(visitType);

        if (offset == null) {
            return null;
        }

        LocalDate minDate = vaccinationDate.plusDays(offset.getEarliestDateOffset());
        LocalDate maxDate = vaccinationDate.plusDays(offset.getLatestDateOffset());

        return new Range<>(minDate, maxDate);
    }

    private LocalDate getVaccinationDate(Visit visit, Boolean boosterRelated) {
        if (boosterRelated) {
            return visit.getSubject().getBoosterVaccinationDate();
        } else {
            return visit.getSubject().getPrimerVaccinationDate();
        }
    }

    private Boolean isBoosterRelated(VisitType visitType, List<String> boosterRelatedVisits) {
        return boosterRelatedVisits.contains(visitType.getDisplayValue());
    }
}
