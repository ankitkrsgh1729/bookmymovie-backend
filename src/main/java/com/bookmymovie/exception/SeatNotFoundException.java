package com.bookmymovie.exception;

public class SeatNotFoundException extends TheaterException {
    public SeatNotFoundException(String message) {
        super(message);
    }
}