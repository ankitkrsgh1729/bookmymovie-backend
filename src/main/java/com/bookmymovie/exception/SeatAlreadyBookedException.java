package com.bookmymovie.exception;
public class SeatAlreadyBookedException extends TheaterException {
    public SeatAlreadyBookedException(String message) {
        super(message);
    }
}
