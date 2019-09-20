package org.motechproject.evsmbarara.exception;

public class EvsException extends RuntimeException {

    public EvsException(String message, Throwable cause, String... params) {
        this(String.format(message, params), cause);
    }

    public EvsException(String message, String... params) {
        this(String.format(message, params));
    }

    public EvsException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvsException(String message) {
        super(message);
    }
}
