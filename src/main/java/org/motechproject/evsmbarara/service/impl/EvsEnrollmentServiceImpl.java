package org.motechproject.evsmbarara.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.evsmbarara.domain.Enrollment;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.SubjectEnrollments;
import org.motechproject.evsmbarara.domain.Visit;
import org.motechproject.evsmbarara.domain.enums.EnrollmentStatus;
import org.motechproject.evsmbarara.domain.enums.VisitType;
import org.motechproject.evsmbarara.exception.EvsEnrollmentException;
import org.motechproject.evsmbarara.repository.EnrollmentDataService;
import org.motechproject.evsmbarara.repository.SubjectEnrollmentsDataService;
import org.motechproject.evsmbarara.service.EvsEnrollmentService;
import org.motechproject.messagecampaign.exception.CampaignNotFoundException;
import org.motechproject.messagecampaign.service.MessageCampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("evsEnrollmentService")
public class EvsEnrollmentServiceImpl implements EvsEnrollmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvsEnrollmentServiceImpl.class);

    private SubjectEnrollmentsDataService subjectEnrollmentsDataService;

    private EnrollmentDataService enrollmentDataService;

    private MessageCampaignService messageCampaignService;

    @Override
    public void enrollSubject(String subjectId) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subjectId);
        checkSubjectEnrollmentsStatus(subjectEnrollments, subjectId);

        Subject subject = subjectEnrollments.getSubject();

        for (Enrollment enrollment : subjectEnrollments.getEnrollments()) {
            if (!EnrollmentStatus.UNENROLLED.equals(enrollment.getStatus())
                && !EnrollmentStatus.INITIAL.equals(enrollment.getStatus())) {
                enrollment.setPreviousStatus(EnrollmentStatus.ENROLLED);
                continue;
            }
            if (enrollment.getReferenceDate() == null) {
                throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because reference date is empty",
                        subject.getSubjectId(), enrollment.getCampaignName());
            }

            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            scheduleJobsForEnrollment(enrollment, true);
            enrollmentDataService.update(enrollment);
        }
        updateSubjectEnrollments(subjectEnrollments);
    }

    @Override
    public void enrollSubjectToCampaign(String subjectId, String campaignName) {
        enrollUnenrolled(subjectId, campaignName, null);
    }

    @Override
    public void enrollOrReenrollSubject(Subject subject) {
        for (Visit visit: subject.getVisits()) {
            if (VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())) {
                enrollOrReenrollSubject(subject, visit.getType().getDisplayValue(), visit.getDate());
            } else if (visit.getDate() == null && visit.getDateProjected() != null) {
                enrollOrReenrollSubject(subject, visit.getType().getDisplayValue(), visit.getDateProjected());
            }
        }
    }

    @Override
    public void unenrollSubject(String subjectId) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subjectId);
        if (subjectEnrollments == null || !EnrollmentStatus.ENROLLED.equals(subjectEnrollments.getStatus())) {
            throw new EvsEnrollmentException("Cannot unenroll Participant, because Participant with id: %s is not enrolled in any campaign",
                    subjectId);
        }
        for (Enrollment enrollment: subjectEnrollments.getEnrollments()) {
            if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
                unscheduleJobsForEnrollment(enrollment);

                enrollment.setStatus(EnrollmentStatus.UNENROLLED);
                enrollmentDataService.update(enrollment);
            } else {
                enrollment.setPreviousStatus(EnrollmentStatus.UNENROLLED);
            }
        }
        subjectEnrollments.setDateOfUnenrollment(LocalDate.now());
        updateSubjectEnrollments(subjectEnrollments);
    }

    @Override
    public void unenrollSubject(String subjectId, String campaignName) {
        if (!unscheduleJobsAndSetStatusForEnrollment(subjectId, campaignName, EnrollmentStatus.UNENROLLED)) {
            throw new EvsEnrollmentException("Cannot unenroll Participant, because no Participant with id: %s registered in Campaign with name: %s",
                    subjectId, campaignName);
        }
    }

    @Override
    public void completeCampaign(String subjectId, String campaignName) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subjectId);
        if (subjectEnrollments != null) {
            Enrollment enrollment = subjectEnrollments.findEnrolmentByCampaignName(campaignName);
            if (enrollment != null) {
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
                updateSubjectEnrollments(subjectEnrollments);
            }
        }
    }

    @Override
    public void completeCampaign(Visit visit) {
        try {
            unscheduleJobsAndSetStatusForEnrollment(visit.getSubject().getSubjectId(), visit.getType().getDisplayValue(), EnrollmentStatus.COMPLETED);
        } catch (EvsEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public void createEnrollmentOrReenrollCampaign(Visit visit, boolean rollbackCompleted) {
        Subject subject = visit.getSubject();

        if (requiredDataMissing(subject)) {
            return;
        }

        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subject.getSubjectId());

        if (subjectEnrollments == null) {
            subjectEnrollments = new SubjectEnrollments(subject);
        }

        String campaignName = visit.getType().getDisplayValue();
        LocalDate referenceDate = visit.getDateProjected();
        if (VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())) {
            referenceDate = visit.getDate();
        }

        Enrollment enrollment = subjectEnrollments.findEnrolmentByCampaignName(campaignName);

        try {
            if (enrollment == null) {
                enrollment = new Enrollment(subject.getSubjectId(), campaignName, referenceDate, EnrollmentStatus.ENROLLED);
                subjectEnrollments.addEnrolment(enrollment);

                scheduleJobsForEnrollment(enrollment, false);
            } else if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
                reenrollSubjectWithNewDate(subject.getSubjectId(), enrollment.getCampaignName(), referenceDate);
            } else if (!EnrollmentStatus.COMPLETED.equals(enrollment.getStatus())) {
                updateReferenceDateIfUnenrolled(enrollment, referenceDate);
            } else if (rollbackCompleted && EnrollmentStatus.COMPLETED.equals(enrollment.getStatus())) {
                enrollment.setStatus(EnrollmentStatus.ENROLLED);
                enrollment.setReferenceDate(referenceDate);

                scheduleJobsForEnrollment(enrollment, true);
            }

            updateSubjectEnrollments(subjectEnrollments);
        } catch (EvsEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public void unenrollAndRemoveEnrollment(Visit visit) {
        unenrollAndRemoveEnrollment(visit.getSubject().getSubjectId(), visit.getType().getDisplayValue());
    }

    @Override
    public void unenrollAndRemoveEnrollment(Subject subject) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subject.getSubjectId());

        if (subjectEnrollments != null) {
            try {
                for (Enrollment enrollment : subjectEnrollments.getEnrollments()) {
                    if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
                        unscheduleJobsForEnrollment(enrollment);
                    }
                    enrollmentDataService.delete(enrollment);
                }

                subjectEnrollmentsDataService.delete(subjectEnrollments);
            } catch (EvsEnrollmentException e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }
    }

    private void reenrollSubjectWithNewDate(String subjectId, String campaignName, LocalDate date) {
        unenrollSubject(subjectId, campaignName);
        enrollUnenrolled(subjectId, campaignName, date);
    }

    private void unenrollAndRemoveEnrollment(String subjectId, String campaignName) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subjectId);

        if (subjectEnrollments != null) {
            Enrollment enrollment = subjectEnrollments.findEnrolmentByCampaignName(campaignName);

            if (enrollment != null) {
                unenrollAndDeleteEnrollment(enrollment, subjectEnrollments);
            }

            if (subjectEnrollments.getEnrollments().isEmpty()) {
                subjectEnrollmentsDataService.delete(subjectEnrollments);
            }
        }
    }

    private void unenrollAndDeleteEnrollment(Enrollment enrollment, SubjectEnrollments subjectEnrollments) {
        try {
            if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
                unscheduleJobsForEnrollment(enrollment);
            }

            subjectEnrollments.removeEnrolment(enrollment);
            updateSubjectEnrollments(subjectEnrollments);
            enrollmentDataService.delete(enrollment);
        } catch (EvsEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    private void updateReferenceDateIfUnenrolled(Enrollment enrollment, LocalDate referenceDate) {
        enrollment.setReferenceDate(referenceDate);
        enrollmentDataService.update(enrollment);
    }

    private void enrollOrReenrollSubject(Subject subject, String campaignName, LocalDate referenceDate) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subject.getSubjectId());

        if (subjectEnrollments == null) {
            enrollNew(subject, campaignName, referenceDate);
        } else {
            Enrollment enrollment = subjectEnrollments.findEnrolmentByCampaignName(campaignName);

            if (enrollment == null) {
                enrollNew(subject, campaignName, referenceDate);
            } else if (!enrollment.getReferenceDate().equals(referenceDate)) {
                try {
                    if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
                        reenrollSubjectWithNewDate(subject.getSubjectId(), enrollment.getCampaignName(), referenceDate);
                    } else {
                        updateReferenceDateIfUnenrolled(enrollment, referenceDate);
                    }
                } catch (EvsEnrollmentException e) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }
    }

    private void enrollNew(Subject subject, String campaignName, LocalDate referenceDate) {
        try {
            enrollNew(subject, campaignName, referenceDate, null);
        } catch (EvsEnrollmentException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    private void enrollNew(Subject subject, String campaignName, LocalDate referenceDate, Time deliverTime) {
        if (subject == null) {
            throw new EvsEnrollmentException("Cannot enroll Participant to Campaign with name: %s, because participant is null",
                    campaignName);
        }

        if (referenceDate == null) {
            throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because reference date is empty",
                    subject.getSubjectId(), campaignName);
        }

        if (requiredDataMissing(subject)) {
            return;
        }

        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subject.getSubjectId());

        if (subjectEnrollments == null) {
            subjectEnrollments = new SubjectEnrollments(subject);
        }

        Enrollment enrollment = subjectEnrollments.findEnrolmentByCampaignName(campaignName);

        if (enrollment != null) {
            throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because enrollment already exists",
                   subject.getSubjectId(), campaignName);
        }

        enrollment = new Enrollment(subject.getSubjectId(), campaignName, referenceDate, deliverTime);

        subjectEnrollments.addEnrolment(enrollment);

        scheduleJobsForEnrollment(enrollment, false);

        updateSubjectEnrollments(subjectEnrollments);
    }

    private void enrollUnenrolled(String subjectId, String campaignName, LocalDate referenceDate) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subjectId);
        if (subjectEnrollments == null) {
            throw new EvsEnrollmentException("Cannot enroll Participant, because not found unenrolled Participant with id: %s in campaign with name: %s",
                    subjectId, campaignName);
        }

        Enrollment enrollment = subjectEnrollments.findEnrolmentByCampaignName(campaignName);

        checkIfUnenrolled(enrollment, subjectId, campaignName);

        if (referenceDate != null) {
            enrollment.setReferenceDate(referenceDate);
        } else if (enrollment.getReferenceDate() == null) {
            throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because reference date is empty",
                    subjectId, enrollment.getCampaignName());
        }

        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        scheduleJobsForEnrollment(enrollment, true);

        updateSubjectEnrollments(subjectEnrollments);
    }

    private void checkIfUnenrolled(Enrollment enrollment, String subjectId, String campaignName) {
        if (enrollment == null) {
            throw new EvsEnrollmentException("Cannot enroll Participant, because not found unenrolled Participant with id: %s in campaign with name: %s",
                    subjectId, campaignName);
        }
        if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
            throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because participant is already enrolled in this campaign",
                    subjectId, enrollment.getCampaignName());
        }
        if (EnrollmentStatus.COMPLETED.equals(enrollment.getStatus())) {
            throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because this campaign is completed",
                    subjectId, enrollment.getCampaignName());
        }
    }

    private boolean unscheduleJobsAndSetStatusForEnrollment(String subjectId, String campaignName, EnrollmentStatus status) {
        SubjectEnrollments subjectEnrollments = subjectEnrollmentsDataService.findBySubjectId(subjectId);
        if (subjectEnrollments == null) {
            return false;
        }
        Enrollment enrollment = subjectEnrollments.findEnrolmentByCampaignName(campaignName);

        if (enrollment == null) {
            return false;
        } else if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
            unscheduleJobsForEnrollment(enrollment);

            enrollment.setStatus(status);
            updateSubjectEnrollments(subjectEnrollments);

            return true;
        } else if ((EnrollmentStatus.UNENROLLED.equals(enrollment.getStatus()) || EnrollmentStatus.INITIAL.equals(enrollment.getStatus())) && !status.equals(enrollment.getStatus())) {
            enrollment.setStatus(status);
            updateSubjectEnrollments(subjectEnrollments);
        }

        return false;
    }

    private void scheduleJobsForEnrollment(Enrollment enrollment, boolean completeIfLastMessageInThePast) {
        try {
            messageCampaignService.scheduleJobsForEnrollment(enrollment.toCampaignEnrollment());
        } catch (CampaignNotFoundException e) {
            throw new EvsEnrollmentException("Cannot enroll Participant with id: %s because Campaign with name: %s doesn't exist",
                    e, enrollment.getExternalId(), enrollment.getCampaignName());
        } catch (IllegalArgumentException e) {
            if (completeIfLastMessageInThePast) {
                LOGGER.debug("Cannot enroll Participant with id: {} for Campaign with name: {}, because last message date is in the past. Changing enrollment status to Completed",
                        enrollment.getExternalId(), enrollment.getCampaignName(), e);
                enrollment.setStatus(EnrollmentStatus.COMPLETED);
            } else {
                throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because last message date is in the past",
                        e, enrollment.getExternalId(), enrollment.getCampaignName());
            }
        } catch (Exception e) {
            throw new EvsEnrollmentException("Cannot enroll Participant with id: %s to Campaign with name: %s, because of unknown exception",
                    e, enrollment.getExternalId(), enrollment.getCampaignName());
        }
    }

    private void unscheduleJobsForEnrollment(Enrollment enrollment) {
        try {
            messageCampaignService.unscheduleJobsForEnrollment(enrollment.toCampaignEnrollment());
        } catch (CampaignNotFoundException e) {
            throw new EvsEnrollmentException("Cannot unenroll Participant with id: %s because campaign with name: %s doesn't exist",
                    e, enrollment.getExternalId(), enrollment.getCampaignName());
        } catch (Exception e) {
            throw new EvsEnrollmentException("Cannot unenroll Participant with id: %s from campaign with name: %s, because of unknown exception",
                    e, enrollment.getExternalId(), enrollment.getCampaignName());
        }
    }

    private void updateSubjectEnrollments(SubjectEnrollments subjectEnrollments) {
        EnrollmentStatus status = EnrollmentStatus.COMPLETED;

        for (Enrollment enrollment : subjectEnrollments.getEnrollments()) {
            if (EnrollmentStatus.ENROLLED.equals(enrollment.getStatus())) {
                status = EnrollmentStatus.ENROLLED;
                break;
            }
            if (EnrollmentStatus.UNENROLLED.equals(enrollment.getStatus())) {
                status = EnrollmentStatus.UNENROLLED;
            } else if (!EnrollmentStatus.UNENROLLED.equals(status) && EnrollmentStatus.INITIAL.equals(enrollment.getStatus())) {
                status = EnrollmentStatus.INITIAL;
            }
        }

        subjectEnrollments.setStatus(status);
        subjectEnrollmentsDataService.update(subjectEnrollments);
    }

    private void checkSubjectEnrollmentsStatus(SubjectEnrollments subjectEnrollments, String subjectId) {
        if (subjectEnrollments == null || !(EnrollmentStatus.UNENROLLED.equals(subjectEnrollments.getStatus())
            || EnrollmentStatus.INITIAL.equals(subjectEnrollments.getStatus()))) {
            throw new EvsEnrollmentException("Cannot enroll Participant, because no unenrolled Participant exist with id: %s",
                    subjectId);
        }
    }

    private boolean requiredDataMissing(Subject subject) {
        if (StringUtils.isBlank(subject.getPhoneNumber())) {
            return true;
        }

        return subject.getLanguage() == null;
    }

    @Autowired
    public void setSubjectEnrollmentsDataService(SubjectEnrollmentsDataService subjectEnrollmentsDataService) {
        this.subjectEnrollmentsDataService = subjectEnrollmentsDataService;
    }

    @Autowired
    public void setEnrollmentDataService(EnrollmentDataService enrollmentDataService) {
        this.enrollmentDataService = enrollmentDataService;
    }

    @Autowired
    public void setMessageCampaignService(MessageCampaignService messageCampaignService) {
        this.messageCampaignService = messageCampaignService;
    }
}
