package org.kuplkris.accounts.exception;

/**
 * This exception is used when we tried to get database connection several times and didn't get it.
 */

public class ConnectionAcquireTimeOutException extends Exception {

    public ConnectionAcquireTimeOutException(final String message) {
        super(message);
    }

    public ConnectionAcquireTimeOutException(final Throwable cause) {
        super(cause);
    }

    public ConnectionAcquireTimeOutException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
