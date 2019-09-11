package org.motechproject.evsmbarara.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Screening;
import org.motechproject.evsmbarara.domain.Volunteer;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;
import org.motechproject.evsmbarara.dto.ScreeningDto;
import org.motechproject.evsmbarara.exception.LimitationExceededException;
import org.motechproject.evsmbarara.helper.VisitLimitationHelper;
import org.motechproject.evsmbarara.mapper.ScreeningMapper;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.ScreeningDataService;
import org.motechproject.evsmbarara.repository.VolunteerDataService;
import org.motechproject.evsmbarara.service.LookupService;
import org.motechproject.evsmbarara.service.ScreeningService;
import org.motechproject.evsmbarara.util.QueryParamsBuilder;
import org.motechproject.evsmbarara.validation.ScreeningValidator;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("screeningService")
public class ScreeningServiceImpl implements ScreeningService {

    @Autowired
    private ScreeningDataService screeningDataService;

    @Autowired
    private VolunteerDataService volunteerDataService;

    @Autowired
    private ClinicDataService clinicDataService;

    @Autowired
    private VisitLimitationHelper visitLimitationHelper;

    @Autowired
    private LookupService lookupService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Records<Screening> getScreenings(GridSettings gridSettings) throws IOException {
        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(gridSettings, getFields(gridSettings.getFields()));
        return lookupService.getEntities(Screening.class, gridSettings.getLookup(), gridSettings.getFields(), queryParams);
    }

    @Override
    public Screening addOrUpdate(ScreeningDto screeningDto, Boolean ignoreLimitation) {
        if (screeningDto.getId() != null) {
            return update(screeningDto, ignoreLimitation);
        }
        return add(screeningDto, ignoreLimitation);
    }

    @Override
    public ScreeningDto getScreeningById(Long id) {
        return ScreeningMapper.INSTANCE.toDto(screeningDataService.findById(id));
    }

    @Override
    public void cancelScreening(Long id) {
        Screening screening = screeningDataService.findById(id);

        if (screening != null) {
            screening.setStatus(ScreeningStatus.CANCELED);
            screeningDataService.update(screening);
        }
    }

    @Override
    public void completeScreening(Long id) {
        Screening screening = screeningDataService.findById(id);

        if (screening != null) {
            screening.setStatus(ScreeningStatus.COMPLETED);
            screeningDataService.update(screening);
        }
    }

    @Override
    public void activateScreening(Long id, Boolean ignoreLimitation) {
        Screening screening = screeningDataService.findById(id);

        if (screening != null) {
            screening.setStatus(ScreeningStatus.ACTIVE);

            if (screening.getClinic() != null) {
                checkNumberOfPatients(screening.getClinic(), screening.getDate(), screening.getStartTime(), screening.getEndTime(), screening, ignoreLimitation);
            }

            screeningDataService.update(screening);
        }
    }

    private Volunteer setVolunteer(ScreeningDto dto) {
        Volunteer volunteer;
        String volunteerId = dto.getVolunteerId();
        if (StringUtils.isNotBlank(volunteerId)) {
            volunteer = volunteerDataService.findById(Long.valueOf(volunteerId));
        } else {
            volunteer = new Volunteer();
        }
        volunteer.setName(dto.getName());
        volunteer.setContactNumber(dto.getContactNumber());
        volunteer.setAdditionalContact(dto.getAdditionalContact());
        return volunteerDataService.createOrUpdate(volunteer);
    }

    private Screening add(ScreeningDto screeningDto, Boolean ignoreLimitation) {

        ScreeningValidator.validateForAdd(screeningDto);

        Screening screening = new Screening();

        checkNumberOfPatientsAndSetScreeningData(screeningDto, screening, ignoreLimitation);

        return screeningDataService.create(screening);
    }

    private Screening update(ScreeningDto screeningDto, Boolean ignoreLimitation) {

        ScreeningValidator.validateForUpdate(screeningDto);

        Long screeningId = Long.parseLong(screeningDto.getId());
        Screening screening = screeningDataService.findById(screeningId);

        Validate.notNull(screening, String.format("Screening with id \"%s\" doesn't exist!", screeningId));

        checkNumberOfPatientsAndSetScreeningData(screeningDto, screening, ignoreLimitation);

        return screeningDataService.update(screening);
    }


    private void checkNumberOfPatientsAndSetScreeningData(ScreeningDto screeningDto, Screening screening, Boolean ignoreLimitation) {
        Clinic clinic = clinicDataService.findById(Long.parseLong(screeningDto.getClinicId()));
        LocalDate date = LocalDate.parse(screeningDto.getDate());
        Time startTime;
        Time endTime;
        if (StringUtils.isNotBlank(screeningDto.getStartTime())) {
            startTime = Time.valueOf(screeningDto.getStartTime());
            endTime = calculateEndTime(startTime);
        } else {
            startTime = null;
            endTime = null;
        }

        checkNumberOfPatients(clinic, date, startTime, endTime, screening, ignoreLimitation);

        screening.setDate(date);
        screening.setStartTime(startTime);
        screening.setEndTime(endTime);
        screening.setClinic(clinic);
        screening.setVolunteer(setVolunteer(screeningDto));
    }

    private void checkNumberOfPatients(Clinic clinic, LocalDate date, Time startTime, Time endTime, Screening screening, Boolean ignoreLimitation) { //NO CHECKSTYLE CyclomaticComplexity
        if (!ignoreLimitation && !ScreeningStatus.CANCELED.equals(screening.getStatus())) {
            visitLimitationHelper.checkCapacityForScreening(date, clinic, screening.getId());
            List<Screening> screeningList = screeningDataService.findByClinicIdAndDateAndStatus(clinic.getId(), date, ScreeningStatus.ACTIVE);

            if (screeningList != null) {
                Integer numberOfRooms = clinic.getNumberOfRooms();
                int maxVisits = clinic.getMaxScreeningVisits();
                int patients = 0;

                for (Screening s : screeningList) {
                    if (s.getId().equals(screening.getId())) {
                        maxVisits++;
                    } else if (startTime != null && s.getStartTime() != null) {
                        if (startTime.isBefore(s.getStartTime())) {
                            if (s.getStartTime().isBefore(endTime)) {
                                patients++;
                            }
                        } else {
                            if (startTime.isBefore(s.getEndTime())) {
                                patients++;
                            }
                        }
                    }
                }

                if (screeningList.size() >= maxVisits) {
                    throw new LimitationExceededException("The booking limit for this type of visit has been reached");
                }
                if (numberOfRooms != null && patients >= numberOfRooms) {
                    throw new LimitationExceededException("Too many visits at the same time");
                }
            }
        }
    }

    private Map<String, Object> getFields(String json) throws IOException {
        if (json == null) {
            return null;
        } else {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }

    private Time calculateEndTime(Time startTime) {
        int endTimeHour = (startTime.getHour() + EvsMbararaConstants.TIME_OF_THE_VISIT) % EvsMbararaConstants.MAX_TIME_HOUR;
        return new Time(endTimeHour, startTime.getMinute());
    }
}
