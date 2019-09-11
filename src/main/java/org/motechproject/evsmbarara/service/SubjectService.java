package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;

import java.util.List;

/**
 * Service interface for CRUD on Subject
 */
public interface SubjectService {

    Subject createOrUpdateForZetes(SubjectZetesDto newSubject);

    Subject findSubjectBySubjectId(String subjectId);

    List<Subject> findModifiedSubjects();

    Subject create(Subject record);

    Subject update(Subject record);
}
