package com.vertex.pm.exception;

public class ProjectManagementException extends Exception {
    private final ExceptionType type;
    private final int statusCode;

    public ProjectManagementException(ExceptionType type, String message, int statusCode) {
        super(message);
        this.type = type;
        this.statusCode = statusCode;
    }

    public ProjectManagementException(ExceptionType type, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.statusCode = statusCode;
    }

    public ExceptionType getType() {
        return type;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
