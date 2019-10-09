package org.motechproject.evsmbarara.service.impl;

import java.util.Objects;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.repository.SubjectDataService;
import org.motechproject.evsmbarara.service.SubjectService;
import org.motechproject.evsmbarara.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link org.motechproject.evsmbarara.service.SubjectService} interface. Uses
 * {@link org.motechproject.evsmbarara.repository.SubjectDataService} in order to retrieve and persist records.
 */
@Service("subjectService")
public class SubjectServiceImpl implements SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Autowired
    private SubjectDataService subjectDataService;

    @Autowired
    private VisitService visitService;

    @Override
    public Subject findSubjectBySubjectId(String subjectId) {
        return subjectDataService.findBySubjectId(subjectId);
    }

    @Override
    public Subject create(Subject subject) {
        subjectDataService.create(subject);
        visitService.createVisitsForSubject(subject);

        return subject;
    }

    @Override
    public Subject update(Subject subject) {
        subjectDataChanged(subject);

        return subjectDataService.update(subject);
    }

    @Override
    public Subject update(Subject subject, Subject oldSubject) {
        subjectDataChanged(subject, oldSubject, subject);

        return subjectDataService.update(subject);
    }

    @Override
    public void subjectDataChanged(Subject subject) {
        Subject oldSubject = findSubjectBySubjectId(subject.getSubjectId());
        subjectDataChanged(subject, oldSubject, oldSubject);
    }

    //CHECKSTYLE:OFF: checkstyle:cyclomaticcomplexity
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private void subjectDataChanged(Subject newSubject, Subject oldSubject, Subject subject) {
        if (oldSubject != null) {
            if ((oldSubject.getSubStudy() == null || !oldSubject.getSubStudy())
                && newSubject.getSubStudy() != null && newSubject.getSubStudy()) {
                visitService.createSubStudyVisits(subject);
            } else if ((newSubject.getSubStudy() == null || !newSubject.getSubStudy())
                && oldSubject.getSubStudy() != null && oldSubject.getSubStudy()) {
                visitService.removeSubStudyVisits(subject);
            }

            if (oldSubject.getPrimerVaccinationDate() != null && newSubject.getPrimerVaccinationDate() == null) {
                subject.setPrimerVaccinationDate(newSubject.getPrimerVaccinationDate());
                visitService.removeVisitsPlannedDates(subject);
            } else if (!Objects.equals(oldSubject.getPrimerVaccinationDate(), newSubject.getPrimerVaccinationDate())) {
                subject.setPrimerVaccinationDate(newSubject.getPrimerVaccinationDate());
                visitService.calculateVisitsPlannedDates(subject);
            }

            if (!Objects.equals(oldSubject.getBoosterVaccinationDate(), newSubject.getBoosterVaccinationDate())) {
                subject.setPrimerVaccinationDate(newSubject.getPrimerVaccinationDate());
                subject.setBoosterVaccinationDate(newSubject.getBoosterVaccinationDate());
                visitService.recalculateBoostRelatedVisitsPlannedDates(subject);
            }
        }
    }
    //CHECKSTYLE:ON: checkstyle:cyclomaticcomplexity
}
