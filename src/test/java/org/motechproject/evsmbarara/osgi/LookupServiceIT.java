package org.motechproject.evsmbarara.osgi;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.repository.VisitDataService;
import org.motechproject.evsmbarara.service.LookupService;
import org.motechproject.evsmbarara.utils.VisitUtil;
import org.motechproject.evsmbarara.web.domain.Records;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.motechproject.evsmbarara.utils.ClinicUtil.createClinic;
import static org.motechproject.evsmbarara.utils.SubjectUtil.createSubject;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LookupServiceIT extends BasePaxIT {

    @Inject
    private SubjectDataService subjectDataService;

    @Inject
    private VisitDataService visitDataService;

    @Inject
    private LookupService lookupService;

    @Inject
    private ClinicDataService clinicDataService;

    private ArrayList<Visit> testVisits = new ArrayList<Visit>();

    @Before
    public void cleanBefore() {
        cleanDatabase();
        resetTestFields();
    }

    @Before
    public void cleanAfter() {
        cleanDatabase();
    }

    @Test
    public void shouldGetVisitEntitiesFromLookup() {
        addTestVisitsToDB();
        String []fields = {
                "{\"type\":\"SCREENING\"}",
                "{\"subject.subjectId\":\"1000000\"}",
                "{\"subject.subjectId\":\"1000000162\"}",
                "{\"clinic.location\":\"location1\"}",
                "{\"subject.name\":\"Michal\"}",
                "{\"dateProjected\":{\"min\":\"2014-10-16\",\"max\":\"2014-10-23\"}}",
                "{\"date\":{\"min\":\"2014-10-16\",\"max\":\"2014-10-21\"}}"
        };
        String []lookups = {
                "Find By Visit Type",
                "Find By Participant Id",
                "Find By exact Participant Id",
                "Find By Clinic Location",
                "Find By Participant Name",
                "Find By Visit Planned Date Range",
                "Find By Visit Actual Date Range"
        };
        int []expectedResults = {2, 4, 2, 2, 2, 3, 3};

        QueryParams queryParams = new QueryParams(1, null);
        for (int i = 0; i < lookups.length; i++) {
            Records<Visit> records = lookupService.getEntities(Visit.class, lookups[i], fields[i], queryParams);
            List<Visit> visitList = records.getRows();
            assertEquals(expectedResults[i], visitList.size());
        }
    }

    @Test
    public void shouldGetSubjectEntitiesFromLookup() {
        addTestVisitsToDB();
        String []fields = {
                "{\"primerVaccinationDate\":\"2014-10-16\"}",
                "{\"boosterVaccinationDate\":{\"min\":\"2014-10-15\",\"max\":\"2014-10-18\"}}",
                "{\"name\":\"Michal\"}",
                "{\"boosterVaccinationDate\":\"2014-10-16\"}",
                "{\"changed\":\"false\"}",
                "{\"address\":\"address\"}"
        };
        String []lookups = {
                "Find By Primer Vaccination Date",
                "Find By Booster Vaccination Date Range",
                "Find By Name",
                "Find By Booster Vaccination Date",
                "Find By Modified",
                "Find By Address",
        };
        int []expectedResults = {1, 2, 1, 1, 2, 2};

        QueryParams queryParams = new QueryParams(1, null);
        for (int i = 0; i < lookups.length; i++) {
            Records<Subject> records = lookupService.getEntities(Subject.class, lookups[i], fields[i], queryParams);
            List<Subject> subjectList = records.getRows();
            assertEquals(expectedResults[i], subjectList.size());
        }
    }

    private void resetTestFields() {
        Subject firstSubject = createSubject("1000000161", "Michal", "729402018364", Language.English);

        Subject secondSubject = createSubject("1000000162", "Rafal", "44443333222", Language.Susu);

        firstSubject.setYearOfBirth(1967);
        firstSubject.setGender(Gender.Male);
        firstSubject.setPrimerVaccinationDate(new LocalDate(2014, 10, 16));
        firstSubject.setBoosterVaccinationDate(new LocalDate(2014, 10, 16));

        secondSubject.setYearOfBirth(2005);
        secondSubject.setGender(Gender.Male);
        secondSubject.setPrimerVaccinationDate(new LocalDate(2014, 10, 17));
        secondSubject.setBoosterVaccinationDate(new LocalDate(2014, 10, 17));

        Clinic clinic1 = createClinic("site1", "location1");
        clinicDataService.create(clinic1);
        Clinic clinic2 = createClinic("site2", "location2");
        clinicDataService.create(clinic2);

        testVisits.add(VisitUtil.createVisit(firstSubject, VisitType.SCREENING,
                new LocalDate(2014, 10, 17), new LocalDate(2014, 10, 21), "owner", clinic1));

        testVisits.add(VisitUtil.createVisit(secondSubject, VisitType.SCREENING,
                new LocalDate(2014, 10, 19), new LocalDate(2014, 10, 21), "owner", clinic2));

        testVisits.add(VisitUtil.createVisit(secondSubject, VisitType.PRIME_VACCINATION_FIRST_FOLLOW_UP_VISIT,
                new LocalDate(2014, 10, 21), new LocalDate(2014, 10, 23), "owner", clinic2));

        testVisits.add(VisitUtil.createVisit(firstSubject, VisitType.BOOST_VACCINATION_DAY,
                new LocalDate(2014, 10, 22), new LocalDate(2014, 10, 24), "owner", clinic1));
    }

    private void addTestVisitsToDB() {
        assertEquals(0, subjectDataService.retrieveAll().size());
        assertEquals(0, visitDataService.retrieveAll().size());

        for (Visit visit : testVisits) {
            visitDataService.create(visit);
        }

        assertEquals(2, subjectDataService.retrieveAll().size());
        assertEquals(4, visitDataService.retrieveAll().size());
    }

    private void cleanDatabase() {
        visitDataService.deleteAll();
        subjectDataService.deleteAll();
        clinicDataService.deleteAll();
    }
}
