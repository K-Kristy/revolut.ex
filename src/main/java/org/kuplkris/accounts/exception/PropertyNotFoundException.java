package org.kuplkris.accounts.exception;

/**
 * Exception is used when some properties are absent in the property file
 */
public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(final String message) {
        super(message);
    }

    public PropertyNotFoundException(final Throwable cause) {
        super(cause);
    }

    public PropertyNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
