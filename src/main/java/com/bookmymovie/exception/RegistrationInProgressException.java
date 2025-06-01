package com.bookmymovie.exception;

public class RegistrationInProgressException extends RuntimeException {

    public RegistrationInProgressException(String message) {
        super(message);
    }

    public RegistrationInProgressException(String message, Throwable cause) {
        super(message, cause);
    }
}
