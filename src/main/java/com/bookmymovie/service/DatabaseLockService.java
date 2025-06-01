package com.bookmymovie.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabaseLockService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean acquireRegistrationLock(String email) {
        try {
            // Generate consistent hash for email
            String lockKey = "user_registration_" + email.toLowerCase();

            // Try to acquire PostgreSQL advisory lock
            Boolean lockAcquired = jdbcTemplate.queryForObject(
                    "SELECT pg_try_advisory_lock(hashtext(?))",
                    Boolean.class,
                    lockKey
            );

            return Boolean.TRUE.equals(lockAcquired);
        } catch (Exception e) {
            return false;
        }
    }
//_${__Random(1,999999)}_${__time()}@jmeter.com
    public void releaseRegistrationLock(String email) {
        try {
            String lockKey = "user_registration_" + email.toLowerCase();
            jdbcTemplate.queryForObject(
                    "SELECT pg_advisory_unlock(hashtext(?))",
                    Boolean.class,
                    lockKey
            );
        } catch (Exception e) {
            // Log but don't throw - locks auto-release on connection close
            log.error("Failed to release lock for email {}: {}", email, e.getMessage());
        }
    }
}