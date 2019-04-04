package org.kuplkris.accounts.exception;

/**
 * Exception is used when request doesn't correspond to requirements
 */
public class RequestValidationException extends Exception {

    public RequestValidationException(final String message) {
        super(message);
    }

    public RequestValidationException(final Throwable cause) {
        super(cause);
    }

    public RequestValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
