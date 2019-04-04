package org.kuplkris.accounts.exception;

public class ApplicationRunningException extends RuntimeException {

    public ApplicationRunningException(final String message) {
        super(message);
    }

    public ApplicationRunningException(final Throwable cause) {
        super(cause);
    }

    public ApplicationRunningException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
