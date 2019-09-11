package org.motechproject.evsmbarara.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.DateFilter;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.dto.CapacityReportDto;
import org.motechproject.evsmbarara.repository.ScreeningDataService;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.impl.ReportServiceImpl;
import org.motechproject.evsmbarara.web.domain.GridSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService = new ReportServiceImpl();

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private ScreeningDataService screeningDataService;

    @Mock
    private LookupService lookupService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldGenerateCapacityInfoReports() {
        GridSettings gridSettings = new GridSettings();
        gridSettings.setPage(1);
        gridSettings.setRows(10);
        gridSettings.setDateFilter(DateFilter.DATE_RANGE);
        gridSettings.setStartDate("2017-1-1");
        gridSettings.setEndDate("2017-1-1");

        List<Clinic> clinics = new ArrayList<>(Arrays.asList(
                createClinic(1L, "clinic1", "location1", 10, 5, 5),
                createClinic(2L, "clinic2", "location2", 20, 12, 8)
        ));

        LocalDate date = new LocalDate(2017, 1, 1);

        when(lookupService.getEntities(eq(Clinic.class), eq(gridSettings), (QueryParams) isNull())).thenReturn(clinics);

        when(visitDataService.countFindByClinicIdAndPlannedVisitDate(clinics.get(0).getId(), date)).thenReturn(1L);
        when(visitDataService.countFindByClinicIdVisitTypeAndPlannedVisitDate(clinics.get(0).getId(), VisitType.PRIME_VACCINATION_DAY, date)).thenReturn(2L);
        when(screeningDataService.countFindByClinicIdAndDateAndStatus(clinics.get(0).getId(), date, ScreeningStatus.ACTIVE)).thenReturn(3L);
        when(unscheduledVisitDataService.countFindByClinicIdAndDate(clinics.get(0).getId(), date)).thenReturn(3L);

        when(visitDataService.countFindByClinicIdAndPlannedVisitDate(clinics.get(1).getId(), date)).thenReturn(2L);
        when(visitDataService.countFindByClinicIdVisitTypeAndPlannedVisitDate(clinics.get(1).getId(), VisitType.PRIME_VACCINATION_DAY, date)).thenReturn(1L);
        when(screeningDataService.countFindByClinicIdAndDateAndStatus(clinics.get(1).getId(), date, ScreeningStatus.ACTIVE)).thenReturn(4L);
        when(unscheduledVisitDataService.countFindByClinicIdAndDate(clinics.get(1).getId(), date)).thenReturn(2L);

        List<CapacityReportDto> expectedReports = new ArrayList<>();

        expectedReports.add(new CapacityReportDto("2017-01-01", clinics.get(0).getLocation(), 10, 3, 2, 3));
        expectedReports.add(new CapacityReportDto("2017-01-01", clinics.get(1).getLocation(), 20, 12, 8, 7));

        List<CapacityReportDto> actualReports = reportService.generateCapacityReports(gridSettings);

        assertEquals(expectedReports.size(), actualReports.size());
        for (int i = 0; i < actualReports.size(); i++) {
            assertEquals(expectedReports.get(i).getLocation(), actualReports.get(i).getLocation());
            assertEquals(expectedReports.get(i).getAvailableCapacity(), actualReports.get(i).getAvailableCapacity());
            assertEquals(expectedReports.get(i).getDate(), actualReports.get(i).getDate());
            assertEquals(expectedReports.get(i).getMaxCapacity(), actualReports.get(i).getMaxCapacity());
            assertEquals(expectedReports.get(i).getScreeningSlotRemaining(), actualReports.get(i).getScreeningSlotRemaining());
            assertEquals(expectedReports.get(i).getVaccineSlotRemaining(), actualReports.get(i).getVaccineSlotRemaining());
        }
    }

    private Clinic createClinic(Long id, String siteId, String location, Integer maxCapacityByDay, Integer maxScreeningVisits, Integer maxPrimeVacVisits) {
        Clinic clinic = new Clinic();
        clinic.setId(id);
        clinic.setSiteId(siteId);
        clinic.setLocation(location);
        clinic.setMaxCapacityByDay(maxCapacityByDay);
        clinic.setMaxScreeningVisits(maxScreeningVisits);
        clinic.setMaxPrimeVisits(maxPrimeVacVisits);
        return clinic;
    }
}
