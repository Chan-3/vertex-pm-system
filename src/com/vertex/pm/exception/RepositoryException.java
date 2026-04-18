package com.vertex.pm.exception;

/**
 * Thrown when a repository operation fails.
 */
public class RepositoryException extends ProjectManagementException {
    /**
     * Creates the exception.
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates the exception.
     */
    public RepositoryException(String message) {
        super(message);
    }
}
