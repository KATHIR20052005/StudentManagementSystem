package com.sms.exception;

/**
 * Thrown when a roll number or e-mail violates a UNIQUE constraint.
 */
public class DuplicateStudentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateStudentException(String message, Throwable cause) {
        super(message, cause);
    }
}
