package org.motechproject.evsmbarara.osgi;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.UnscheduledVisitDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.utils.SubjectUtil;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.motechproject.evsmbarara.utils.ClinicUtil.createClinic;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class EvsMbararaLifecycleListenerIT extends BasePaxIT {

    @Inject
    private ClinicDataService clinicDataService;

    @Inject
    private VisitDataService visitDataService;

    @Inject
    private UnscheduledVisitDataService unscheduledVisitDataService;

    @Before
    public void cleanBefore() {
        clearDataBase();
    }

    @After
    public void cleanAfter() {
        clearDataBase();
    }

    @Test
    public void shouldUpdateclinicInVisits() {
        List<Clinic> clinicList = clinicDataService.retrieveAll();
        List<Visit> visitList = visitDataService.retrieveAll();
        assertTrue(clinicList.isEmpty() && visitList.isEmpty());

        String siteId = "siteItToFind";

        Subject subject = SubjectUtil.createSubject("1000000161", "Michal", "729402018364", Language.English);
        subject.setSiteId(siteId);
        Subject subjectNotInCreatedClinic = SubjectUtil.createSubject("1000000162", "Maciek", "129402018364", Language.English);

        createVisit(subject, VisitType.SCREENING, new LocalDate(2014, 10, 17), new LocalDate(2014, 10, 18));
        createVisit(subject, VisitType.PRIME_VACCINATION_DAY, new LocalDate(2014, 10 , 19), new LocalDate(2014, 10, 20));
        createVisit(subject, VisitType.PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT, new LocalDate(2014, 10, 19), new LocalDate(2014, 10, 20));
        createVisit(subjectNotInCreatedClinic, VisitType.PRIME_VACCINATION_DAY, new LocalDate(2014, 10, 20), new LocalDate(2014, 10, 21));

        Clinic clinic = createClinic(siteId, "location");
        clinic = clinicDataService.create(clinic);

        visitList = visitDataService.retrieveAll();

        for (Visit visit : visitList) {
            if (visit.getSubject().getSubjectId().equals(subject.getSubjectId())) {
                assertEquals(clinic, visit.getClinic());
            } else {
                assertEquals(null, visit.getClinic());
            }
        }
    }

    private Visit createVisit(Subject subject, VisitType visitType, LocalDate date, LocalDate projectedDate) {
        Visit visit = new Visit();
        visit.setSubject(subject);
        visit.setType(visitType);
        visit.setDate(date);
        visit.setDateProjected(projectedDate);
        subject.getVisits().add(visit);
        return visit;
    }

    private void clearDataBase() {
        unscheduledVisitDataService.deleteAll();
        visitDataService.deleteAll();
        clinicDataService.deleteAll();
    }
}
