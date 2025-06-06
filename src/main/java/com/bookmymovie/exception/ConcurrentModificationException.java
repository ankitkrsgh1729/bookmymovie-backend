package com.bookmymovie.exception;

public class ConcurrentModificationException extends TheaterException {
    public ConcurrentModificationException(String message) {
        super(message);
    }
}