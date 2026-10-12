package com.sms.exception;

/**
 * Thrown when an operation targets a student that does not exist.
 */
public class StudentNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StudentNotFoundException(String message) {
        super(message);
    }
}
