package com.bookmymovie.exception;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Base exception class for all custom exceptions in the application
 * Provides common fields and behavior for exception handling
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final LocalDateTime timestamp;
    private final String service;

    public BaseException(String message) {
        super(message);
        this.errorCode = generateErrorCode();
        this.timestamp = LocalDateTime.now();
        this.service = extractServiceName();
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = generateErrorCode();
        this.timestamp = LocalDateTime.now();
        this.service = extractServiceName();
    }

    public BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.service = extractServiceName();
    }

    public BaseException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.service = extractServiceName();
    }

    /**
     * Generate error code based on exception class name
     * Example: UserNotFoundException -> USER_NOT_FOUND
     */
    private String generateErrorCode() {
        String className = this.getClass().getSimpleName();

        // Remove "Exception" suffix if present
        if (className.endsWith("Exception")) {
            className = className.substring(0, className.length() - 9);
        }

        // Convert CamelCase to UPPER_SNAKE_CASE
        return className.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    /**
     * Extract service name from package structure
     * Example: com.bookmymovie.user.exception -> USER
     */
    private String extractServiceName() {
        String packageName = this.getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");

        // Look for service name in package structure
        for (int i = 0; i < parts.length - 1; i++) {
            if ("bookmymovie".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1].toUpperCase();
            }
        }

        return "UNKNOWN";
    }

    /**
     * Get formatted error message with timestamp and error code
     */
    public String getFormattedMessage() {
        return String.format("[%s] %s - %s (Error Code: %s)",
                service, timestamp, getMessage(), errorCode);
    }

    /**
     * Check if this exception is retryable
     * Override in specific exceptions as needed
     */
    public boolean isRetryable() {
        return false;
    }

    /**
     * Get HTTP status code for this exception
     * Override in specific exceptions as needed
     */
    public int getHttpStatusCode() {
        return 500; // Internal Server Error by default
    }
}