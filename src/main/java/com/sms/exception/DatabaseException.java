package com.sms.exception;

/**
 * Unchecked wrapper for low-level {@link java.sql.SQLException}s so that upper layers
 * do not depend on JDBC types and are not forced into boilerplate try/catch.
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
