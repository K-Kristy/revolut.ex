package org.kuplkris.accounts.exception;

/**
 * This exception is used because of any database connection's exceptions
 */

public class DatabaseConnectionException extends Exception {

    public DatabaseConnectionException(final String message) {
        super(message);
    }

    public DatabaseConnectionException(final Throwable cause) {
        super(cause);
    }

    public DatabaseConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
