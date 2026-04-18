package com.vertex.pm.exception;

/**
 * Thrown when a required resource is not available.
 */
public class ResourceNotAvailableException extends ProjectManagementException {
    /**
     * Creates the exception.
     */
    public ResourceNotAvailableException(String message) {
        super(message);
    }
}
