package com.vertex.pm.exception;

/**
 * Base exception type for the project management subsystem.
 */
public class ProjectManagementException extends Exception {
    /**
     * Creates the exception with a message.
     */
    public ProjectManagementException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and cause.
     */
    public ProjectManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
