package com.bookmymovie.exception;
public class SeatNotAvailableException extends TheaterException {
    public SeatNotAvailableException(String message) {
        super(message);
    }
}