package com.vertex.pm.exception;

public class RepositoryException extends ProjectManagementException {
    public RepositoryException(String message, Throwable cause) {
        super(ExceptionType.DATABASE_ERROR, message, 500, cause);
    }
}
