package com.vertex.pm.exception;

/**
 * Thrown when a requested project does not exist.
 */
public class ProjectNotFoundException extends ProjectManagementException {
    /**
     * Creates the exception.
     */
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
