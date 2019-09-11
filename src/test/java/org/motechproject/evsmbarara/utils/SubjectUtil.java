package org.motechproject.evsmbarara.utils;

import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;

public final class SubjectUtil {

    private SubjectUtil() {
    }

    public static Subject createSubject(String subjectId, String name, String phoneNumber, Language language) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        subject.setName(name);
        subject.setPhoneNumber(phoneNumber);
        subject.setAddress("address");
        subject.setLanguage(language);
        subject.setCommunity("community");
        subject.setSiteId("B05-SL10001");
        subject.setSiteName("siteName");
        subject.setChiefdom("chiefdom");
        subject.setSection("section");
        subject.setDistrict("district");
        subject.setGender(Gender.Male);
        subject.setAge(45); //NO CHECKSTYLE MagicNumber
        return subject;
    }
}
