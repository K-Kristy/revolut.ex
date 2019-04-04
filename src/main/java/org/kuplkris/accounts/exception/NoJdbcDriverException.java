package org.kuplkris.accounts.exception;

/**
 * Exception is used when we cant find JDBC driver
 */
public class NoJdbcDriverException extends RuntimeException {

    public NoJdbcDriverException(final String message) {
        super(message);
    }

    public NoJdbcDriverException(final Throwable cause) {
        super(cause);
    }

    public NoJdbcDriverException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
