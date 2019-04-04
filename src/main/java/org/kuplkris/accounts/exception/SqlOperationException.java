package org.kuplkris.accounts.exception;

/**
 * Exception is used because of any sql exceptions during sql executions
 */
public class SqlOperationException extends Exception {
    public SqlOperationException(final String message) {
        super(message);
    }

    public SqlOperationException(final Throwable cause) {
        super(cause);
    }

    public SqlOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
