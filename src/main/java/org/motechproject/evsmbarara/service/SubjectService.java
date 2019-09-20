package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.domain.Subject;

/**
 * Service interface for CRUD on Subject
 */
public interface SubjectService {

    Subject findSubjectBySubjectId(String subjectId);

    Subject create(Subject record);

    Subject update(Subject record);

    void subjectDataChanged(Subject subject);
}
