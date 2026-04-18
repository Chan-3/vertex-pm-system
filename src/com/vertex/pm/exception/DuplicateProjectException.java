package com.vertex.pm.exception;

/**
 * Thrown when a duplicate project name is detected.
 */
public class DuplicateProjectException extends ProjectManagementException {
    /**
     * Creates the exception.
     */
    public DuplicateProjectException(String message) {
        super(message);
    }
}
