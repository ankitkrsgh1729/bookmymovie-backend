package com.bookmymovie.service;

import com.bookmymovie.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@Component
@Slf4j
public class OptimisticLockingRetryService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 50;

    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        int attempts = 0;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                attempts++;
                log.debug("Executing {} - Attempt {}/{}", operationName, attempts, MAX_RETRY_ATTEMPTS);

                return operation.get();

            } catch (OptimisticLockingFailureException ex) {
                log.warn("Optimistic locking conflict in {} - Attempt {}/{}",
                        operationName, attempts, MAX_RETRY_ATTEMPTS);

                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error("Max retry attempts reached for {}", operationName);
                    throw new BookingConcurrencyException(
                            "Unable to complete " + operationName + " due to high concurrency. Please try again."
                    );
                }

                try {
                    // Exponential backoff
                    Thread.sleep(RETRY_DELAY_MS * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BookingConcurrencyException("Operation interrupted");
                }
            }
        }

        throw new BookingConcurrencyException("Unexpected error in retry mechanism");
    }

    // Custom exception for concurrency issues
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class BookingConcurrencyException extends BaseException {

        public BookingConcurrencyException(String message) {
            super(message);
        }

        public BookingConcurrencyException(String message, Throwable cause) {
            super(message, cause);
        }

        @Override
        public int getHttpStatusCode() {
            return HttpStatus.CONFLICT.value(); // 409 Conflict
        }
    }
}