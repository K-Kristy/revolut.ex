package org.kuplkris.accounts.exception;

/**
 * Exception in case we cant find application.properties file
 */
public class LoadPropertyFileException extends RuntimeException {

    public LoadPropertyFileException(final String message) {
        super(message);
    }

    public LoadPropertyFileException(final Throwable cause) {
        super(cause);
    }

    public LoadPropertyFileException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
