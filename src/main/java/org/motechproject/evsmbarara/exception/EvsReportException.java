package org.motechproject.evsmbarara.exception;

public class EvsReportException extends EvsException {

    public EvsReportException(String message, Throwable cause, String... params) {
        super(message, cause,  params);
    }

    public EvsReportException(String message, String... params) {
        super(message, params);
    }
}
