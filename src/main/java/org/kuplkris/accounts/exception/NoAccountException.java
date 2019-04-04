package org.kuplkris.accounts.exception;

/**
 * Exception is used if we didn't find account with current Id
 */
public class NoAccountException extends Exception {

    public NoAccountException(final String message) {
        super(message);
    }

    public NoAccountException(final Throwable cause) {
        super(cause);
    }

    public NoAccountException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
