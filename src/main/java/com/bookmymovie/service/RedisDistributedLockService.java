package com.bookmymovie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based distributed locking service using Redisson
 * Provides atomic lock operations across multiple server instances
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisDistributedLockService {

    private final RedissonClient redissonClient;

    /**
     * Attempts to acquire a distributed lock
     *
     * @param lockKey Unique key for the lock
     * @param waitTime Maximum time to wait for the lock
     * @param leaseTime Time after which lock is automatically released
     * @param timeUnit Time unit for waitTime and leaseTime
     * @return DistributedLockResult containing lock status and lock object
     */
    public DistributedLockResult acquireLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        log.debug("Attempting to acquire distributed lock: {}", lockKey);

        try {
            RLock lock = redissonClient.getLock(lockKey);

            // Try to acquire lock with wait and lease time
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);

            if (acquired) {
                log.debug("Successfully acquired distributed lock: {} (lease: {}{})",
                        lockKey, leaseTime, timeUnit.toString().toLowerCase());
                return DistributedLockResult.success(lock, lockKey);
            } else {
                log.warn("Failed to acquire distributed lock: {} (waited: {}{})",
                        lockKey, waitTime, timeUnit.toString().toLowerCase());
                return DistributedLockResult.failure(lockKey, "Lock acquisition timeout");
            }

        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for distributed lock: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return DistributedLockResult.failure(lockKey, "Lock acquisition interrupted");
        } catch (Exception e) {
            log.error("Error acquiring distributed lock: {}", lockKey, e);
            return DistributedLockResult.failure(lockKey, "Lock acquisition error: " + e.getMessage());
        }
    }

    /**
     * Releases a distributed lock
     *
     * @param lock The lock to release
     * @param lockKey The key of the lock (for logging)
     */
    public void releaseLock(RLock lock, String lockKey) {
        if (lock == null) {
            log.warn("Attempted to release null lock for key: {}", lockKey);
            return;
        }

        try {
            // Only release if current thread holds the lock
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Successfully released distributed lock: {}", lockKey);
            } else {
                log.warn("Attempted to release lock not held by current thread: {}", lockKey);
            }
        } catch (Exception e) {
            log.error("Error releasing distributed lock: {}", lockKey, e);
        }
    }

    /**
     * Checks if a lock is currently held
     *
     * @param lockKey The lock key to check
     * @return true if lock is held, false otherwise
     */
    public boolean isLocked(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.isLocked();
        } catch (Exception e) {
            log.error("Error checking lock status: {}", lockKey, e);
            return false;
        }
    }

    /**
     * Gets information about a lock
     *
     * @param lockKey The lock key
     * @return LockInfo containing details about the lock
     */
    public LockInfo getLockInfo(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            return LockInfo.builder()
                    .lockKey(lockKey)
                    .isLocked(lock.isLocked())
                    .holdCount(lock.getHoldCount())
                    .remainingTimeToLive(lock.remainTimeToLive())
                    .isHeldByCurrentThread(lock.isHeldByCurrentThread())
                    .build();
        } catch (Exception e) {
            log.error("Error getting lock info: {}", lockKey, e);
            return LockInfo.builder()
                    .lockKey(lockKey)
                    .isLocked(false)
                    .error("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Result of lock acquisition attempt
     */
    public static class DistributedLockResult {
        private final boolean success;
        private final RLock lock;
        private final String lockKey;
        private final String errorMessage;

        private DistributedLockResult(boolean success, RLock lock, String lockKey, String errorMessage) {
            this.success = success;
            this.lock = lock;
            this.lockKey = lockKey;
            this.errorMessage = errorMessage;
        }

        public static DistributedLockResult success(RLock lock, String lockKey) {
            return new DistributedLockResult(true, lock, lockKey, null);
        }

        public static DistributedLockResult failure(String lockKey, String errorMessage) {
            return new DistributedLockResult(false, null, lockKey, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public RLock getLock() { return lock; }
        public String getLockKey() { return lockKey; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Information about a lock
     */
    @lombok.Builder
    @lombok.Data
    public static class LockInfo {
        private String lockKey;
        private boolean isLocked;
        private int holdCount;
        private long remainingTimeToLive;
        private boolean isHeldByCurrentThread;
        private String error;
    }
}