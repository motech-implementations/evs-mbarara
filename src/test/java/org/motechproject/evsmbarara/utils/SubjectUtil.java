package org.motechproject.evsmbarara.utils;

import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.enums.Language;

public final class SubjectUtil {

    private SubjectUtil() {
    }

    public static Subject createSubject(String subjectId, String name, String phoneNumber, Language language) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        subject.setName(name);
        subject.setPhoneNumber(phoneNumber);
        subject.setLanguage(language);
        return subject;
    }
}
