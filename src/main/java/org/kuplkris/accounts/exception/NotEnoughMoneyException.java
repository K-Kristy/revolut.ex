package org.kuplkris.accounts.exception;

/**
 * Exception is used in case when there are not enough money on to send
 */

public class NotEnoughMoneyException extends Exception {

    public NotEnoughMoneyException(final String message) {
        super(message);
    }

    public NotEnoughMoneyException(final Throwable cause) {
        super(cause);
    }

    public NotEnoughMoneyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
