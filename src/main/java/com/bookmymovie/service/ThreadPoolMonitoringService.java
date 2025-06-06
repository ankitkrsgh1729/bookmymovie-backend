package com.bookmymovie.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;

@Service
@Slf4j
public class ThreadPoolMonitoringService {

    @Autowired(required = false)
    @Qualifier("emailExecutor")
    private ThreadPoolTaskExecutor emailExecutor;

    @Autowired(required = false)
    @Qualifier("analyticsExecutor")
    private ThreadPoolTaskExecutor analyticsExecutor;

    @Autowired(required = false)
    @Qualifier("profileExecutor")
    private ThreadPoolTaskExecutor profileExecutor;

    @Scheduled(fixedRate = 100000) // Every 10 seconds during load testing
    public void logThreadPoolStats() {
        log.info("=== THREAD POOL MONITORING ===");

        if (emailExecutor != null) {
            logExecutorStats("EMAIL", emailExecutor);
        }

        if (analyticsExecutor != null) {
            logExecutorStats("ANALYTICS", analyticsExecutor);
        }

        if (profileExecutor != null) {
            logExecutorStats("PROFILE", profileExecutor);
        }

        // JVM Thread info
        log.info("JVM THREADS - Active: {}, Peak: {}, Total Started: {}",
                Thread.activeCount(),
                ManagementFactory.getThreadMXBean().getPeakThreadCount(),
                ManagementFactory.getThreadMXBean().getTotalStartedThreadCount());
    }

    private void logExecutorStats(String name, ThreadPoolTaskExecutor executor) {
        var threadPoolExecutor = executor.getThreadPoolExecutor();

        log.info("{} POOL - Active: {}/{}, Queue: {}/{}, Completed: {}",
                name,
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getPoolSize(),
                threadPoolExecutor.getQueue().size(),
                executor.getQueueCapacity(),
                threadPoolExecutor.getCompletedTaskCount());
    }
}