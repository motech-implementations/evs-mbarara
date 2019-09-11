package org.motechproject.evsmbarara.service;

import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.impl.ReportServiceImpl;

public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService = new ReportServiceImpl();

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private LookupService lookupService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }
}
