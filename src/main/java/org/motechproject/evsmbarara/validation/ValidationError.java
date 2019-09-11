package org.motechproject.evsmbarara.validation;

import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;

public class ValidationError {

    public static final String SITE_NAME_NULL = "siteName property can't be empty";
    public static final String COMMUNITY_NULL = "community property can't be empty";
    public static final String NAME_NULL = "name property can't be empty";
    public static final String NAME_HAS_DIGITS = "name property cannot contain digits";
    public static final String AGE_NULL = "age property can't be empty";
    public static final String AGE_NEGATIVE = "age property can't be negative";
    public static final String GENDER_NULL = "gender property can't be empty";
    public static final String GENDER_NOT_CORRECT = "gender property can only equal to one of: " +
            Gender.getListOfValues();
    public static final String LANGUAGE_NULL = "language property can't be empty";
    public static final String LANGUAGE_NOT_CORRECT = "language property can only equal to one of: " +
            Language.getListOfCodes();
    public static final String SUBJECT_ID_NULL = "PID property can't be empty";
    public static final String SUBJECT_ID_NOT_VERIFIED = "PID property format verification failed";
    public static final String PHONE_NUMBER_HAS_NON_DIGITS = "Phone number can contain only digits";
    public static final String SITE_ID_NULL = "siteId property can't be empty";

    private String message;

    public ValidationError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return message;
    }
}
