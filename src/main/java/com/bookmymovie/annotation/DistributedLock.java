package com.bookmymovie.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Distributed Lock annotation for Redis-based locking across multiple server instances
 *
 * Usage:
 * @DistributedLock(key = "booking-{#showId}", waitTime = 100, leaseTime = 5000)
 * public BookingResponse bookSeats(Long showId, BookingRequest request) { ... }
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * Lock key expression. Supports SpEL expressions.
     * Examples:
     * - "show-booking-{#showId}"
     * - "user-{#request.userId}-show-{#request.showId}"
     * - "payment-{#bookingReference}"
     */
    String key();

    /**
     * Maximum time to wait for the lock (in milliseconds)
     * Default: 100ms
     */
    long waitTime() default 100;

    /**
     * Time after which the lock will be automatically released (in milliseconds)
     * This prevents deadlocks if the holding process crashes
     * Default: 5000ms (5 seconds)
     */
    long leaseTime() default 5000;

    /**
     * Time unit for waitTime and leaseTime
     * Default: MILLISECONDS
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * Custom error message when lock cannot be acquired
     * Default: "Resource is currently being processed. Please try again."
     */
    String errorMessage() default "Resource is currently being processed. Please try again.";

    /**
     * Whether to throw exception if lock cannot be acquired
     * If false, method will return null or empty response
     * Default: true
     */
    boolean throwExceptionOnFailure() default true;
}