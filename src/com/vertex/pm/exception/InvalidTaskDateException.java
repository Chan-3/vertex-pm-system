package com.vertex.pm.exception;

/**
 * Thrown when task dates are invalid.
 */
public class InvalidTaskDateException extends ProjectManagementException {
    /**
     * Creates the exception.
     */
    public InvalidTaskDateException(String message) {
        super(message);
    }
}
