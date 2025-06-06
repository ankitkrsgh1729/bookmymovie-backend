package com.bookmymovie.exception;
public class ShowNotBookableException extends TheaterException {
    public ShowNotBookableException(String message) {
        super(message);
    }
}