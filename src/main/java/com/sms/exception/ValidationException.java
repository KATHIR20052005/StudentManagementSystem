package com.sms.exception;

/**
 * Thrown when user-supplied data fails business validation.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
