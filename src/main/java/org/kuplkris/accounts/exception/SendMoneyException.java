package org.kuplkris.accounts.exception;

/**
 * Exception is used if there are any issues to send money
 */
public class SendMoneyException extends Exception {

    public SendMoneyException(final String message) {
        super(message);
    }

    public SendMoneyException(final Throwable cause) {
        super(cause);
    }

    public SendMoneyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
