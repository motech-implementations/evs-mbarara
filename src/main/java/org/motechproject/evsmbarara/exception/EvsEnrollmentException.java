package org.motechproject.evsmbarara.exception;

public class EvsEnrollmentException extends EvsException {

    public EvsEnrollmentException(String message, Throwable cause, String... params) {
        super(message, cause, params);
    }

    public EvsEnrollmentException(String message, String... params) {
        super(message, params);
    }
}
