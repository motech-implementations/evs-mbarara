package org.motechproject.evsmbarara.mapper;

import org.apache.commons.lang3.StringUtils;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;

public class EnumsMapper {

    public Gender toGender(String gender) {
        if (StringUtils.isBlank(gender)) {
            return null;
        }

        return Gender.getByValue(gender);
    }

    public String fromGender(Gender gender) {
        if (gender == null) {
            return null;
        }

        return gender.getValue();
    }

    public Language toLanguage(String language) {
        if (StringUtils.isBlank(language)) {
            return null;
        }

        return Language.getByCode(language);
    }

    public String fromLanguage(Language language) {
        if (language == null) {
            return null;
        }

        return language.getCode();
    }
}
