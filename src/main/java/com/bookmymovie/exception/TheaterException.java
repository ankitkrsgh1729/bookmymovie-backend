package com.bookmymovie.exception;


public abstract class TheaterException extends BaseException {
    public TheaterException(String message) {
        super(message);
    }

    public TheaterException(String message, Throwable cause) {
        super(message, cause);
    }
}