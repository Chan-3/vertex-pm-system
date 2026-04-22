package com.vertex.pm.exception;

public class ProjectManagementException extends Exception {
    private final ExceptionType type;
    private final int statusCode;

    /** Creates a business exception with its logical type and HTTP status code. */
    public ProjectManagementException(ExceptionType type, String message, int statusCode) {
        super(message);
        this.type = type;
        this.statusCode = statusCode;
    }

    /** Creates a business exception and preserves the lower-level cause when available. */
    public ProjectManagementException(ExceptionType type, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.statusCode = statusCode;
    }

    /** Returns the logical application error type. */
    public ExceptionType getType() {
        return type;
    }

    /** Returns the HTTP status code that should be sent back to the UI. */
    public int getStatusCode() {
        return statusCode;
    }
}
