package com.bookmymovie.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for distributed locking and caching
 */
@Configuration
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * Redisson client for distributed locking
     */
    @Bean
    public RedissonClient redissonClient() {
        log.info("Configuring Redisson client for Redis at {}:{}", redisHost, redisPort);

        Config config = new Config();

        String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);

        config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(redisDatabase)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(2)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        // Set password if provided
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        RedissonClient redissonClient = Redisson.create(config);

        log.info("Redisson client configured successfully");
        return redissonClient;
    }

    /**
     * Redis template for general Redis operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Health check bean to verify Redis connectivity
     */
    @Bean
    public RedisHealthChecker redisHealthChecker(RedissonClient redissonClient) {
        return new RedisHealthChecker(redissonClient);
    }

    /**
     * Simple health checker for Redis connectivity
     */
    public static class RedisHealthChecker {
        private final RedissonClient redissonClient;

        public RedisHealthChecker(RedissonClient redissonClient) {
            this.redissonClient = redissonClient;
            checkConnection();
        }

        private void checkConnection() {
            try {
                // Simple ping to verify connectivity
                redissonClient.getBucket("health-check").isExists();
                log.info("Redis connectivity verified successfully");
            } catch (Exception e) {
                log.error("Redis connectivity check failed: {}", e.getMessage());
                log.warn("Distributed locking may not work properly without Redis");
            }
        }

        public boolean isHealthy() {
            try {
                return redissonClient.getBucket("health-check").isExists();
            } catch (Exception e) {
                return false;
            }
        }
    }
}