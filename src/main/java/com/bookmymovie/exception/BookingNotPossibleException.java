package com.bookmymovie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BookingNotPossibleException extends BaseException {

    public BookingNotPossibleException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatus.UNPROCESSABLE_ENTITY.value(); // 422
    }
}