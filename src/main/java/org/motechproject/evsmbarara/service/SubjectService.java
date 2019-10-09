package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.domain.Subject;

/**
 * Service interface for CRUD on Subject
 */
public interface SubjectService {

    Subject findSubjectBySubjectId(String subjectId);

    Subject create(Subject subject);

    Subject update(Subject subject);

    Subject update(Subject subject, Subject oldSubject);

    void subjectDataChanged(Subject subject);
}
