package org.motechproject.evsmbarara.service;

import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.Visit;

public interface EvsEnrollmentService {

    void enrollSubject(String subjectId);

    void enrollSubjectToCampaign(String subjectId, String campaignName);

    void enrollOrReenrollSubject(Subject subject);

    void unenrollSubject(String subjectId);

    void unenrollSubject(String subjectId, String campaignName);

    void completeCampaign(Visit visit);

    void completeCampaign(String subjectId, String campaignName);

    void createEnrollmentOrReenrollCampaign(Visit visit);

    void unenrollAndRemoveEnrollment(Visit visit);
}
