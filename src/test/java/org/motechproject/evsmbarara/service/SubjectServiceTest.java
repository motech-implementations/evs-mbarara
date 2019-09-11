package org.motechproject.evsmbarara.service;

import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.impl.SubjectServiceImpl;

public class SubjectServiceTest {

    @InjectMocks
    private SubjectService subjectService = new SubjectServiceImpl();

    @Mock
    private SubjectDataService subjectDataService;

    @Mock
    private VisitDataService visitDataService;

    @Mock
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Before
    public void setUp() {
        initMocks(this);
    }
}
