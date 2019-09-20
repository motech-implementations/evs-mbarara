package org.motechproject.evsmbarara.exception;

public class EvsInitiateCallException extends EvsException {

    public EvsInitiateCallException(String message, Throwable cause, String... params) {
        super(message, cause, params);
    }

    public EvsInitiateCallException(String message, String... params) {
        super(message, params);
    }
}
