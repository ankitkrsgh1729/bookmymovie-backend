package com.bookmymovie.service;

import com.bookmymovie.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimitingService {

    private final ConcurrentMap<String, RateLimitInfo> ipRateLimit = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RateLimitInfo> emailRateLimit = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE_IP = 5;
    private static final int MAX_REQUESTS_PER_HOUR_EMAIL = 3;

    public void checkIpRateLimit(String ipAddress) {
        String key = "ip:" + ipAddress;
        RateLimitInfo info = ipRateLimit.computeIfAbsent(key,
                k -> new RateLimitInfo(MAX_REQUESTS_PER_MINUTE_IP, 1));

        if (!info.tryRequest(MAX_REQUESTS_PER_MINUTE_IP, 1)) {
            throw new RateLimitExceededException(
                    "Too many registration attempts from this IP. Please try again in 1 minute.");
        }
    }

    public void checkEmailRateLimit(String email) {
        String key = "email:" + email.toLowerCase();
        RateLimitInfo info = emailRateLimit.computeIfAbsent(key,
                k -> new RateLimitInfo(MAX_REQUESTS_PER_HOUR_EMAIL, 60));

        if (!info.tryRequest(MAX_REQUESTS_PER_HOUR_EMAIL, 60)) {
            throw new RateLimitExceededException(
                    "Too many registration attempts for this email. Please try again in 1 hour.");
        }
    }

    public void resetEmailRateLimit(String email) {
        String key = "email:" + email.toLowerCase();
        emailRateLimit.remove(key);
    }

    private static class RateLimitInfo {
        private int count;
        private LocalDateTime windowStart;
        private final int windowMinutes;

        public RateLimitInfo(int maxRequests, int windowMinutes) {
            this.count = 1;
            this.windowStart = LocalDateTime.now();
            this.windowMinutes = windowMinutes;
        }

        public synchronized boolean tryRequest(int maxRequests, int windowMinutes) {
            LocalDateTime now = LocalDateTime.now();

            // Reset window if expired
            if (now.isAfter(windowStart.plusMinutes(windowMinutes))) {
                this.count = 1;
                this.windowStart = now;
                return true;
            }

            // Check if limit exceeded
            if (count >= maxRequests) {
                return false;
            }

            count++;
            return true;
        }
    }
}