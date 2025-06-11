package com.bookmymovie;

import com.bookmymovie.annotation.DistributedLock;
import com.bookmymovie.exception.BaseException;
import com.bookmymovie.service.RedisDistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * AOP Aspect to handle @DistributedLock annotation
 * Provides automatic distributed locking around annotated methods
 */
@Aspect
@Component
@Order(1) // Execute before @Transactional
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private final RedisDistributedLockService lockService;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {

        // Generate lock key using SpEL expression
        String lockKey = generateLockKey(distributedLock.key(), joinPoint);

        log.info("Attempting to acquire distributed lock: {} for method: {}",
                lockKey, joinPoint.getSignature().toShortString());

        // Attempt to acquire lock
        RedisDistributedLockService.DistributedLockResult lockResult = lockService.acquireLock(
                lockKey,
                distributedLock.waitTime(),
                distributedLock.leaseTime(),
                distributedLock.timeUnit()
        );

        if (!lockResult.isSuccess()) {
            // Lock acquisition failed
            log.warn("Failed to acquire distributed lock: {} - {}", lockKey, lockResult.getErrorMessage());

            if (distributedLock.throwExceptionOnFailure()) {
                throw new DistributedLockException(distributedLock.errorMessage() + " (Key: " + lockKey + ")");
            } else {
                log.info("Returning null due to lock acquisition failure for key: {}", lockKey);
                return null;
            }
        }

        RLock lock = lockResult.getLock();

        try {
            log.debug("Successfully acquired distributed lock: {}, executing method...", lockKey);

            // Execute the actual method
            Object result = joinPoint.proceed();

            log.debug("Method execution completed successfully for lock: {}", lockKey);
            return result;

        } catch (Exception e) {
            log.error("Error during method execution with distributed lock: {} - {}", lockKey, e.getMessage());
            throw e;
        } finally {
            // Always release the lock
            lockService.releaseLock(lock, lockKey);
            log.debug("Released distributed lock: {}", lockKey);
        }
    }

    /**
     * Generates lock key by parsing SpEL expression with method parameters
     */
    private String generateLockKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        if (keyExpression == null || keyExpression.trim().isEmpty()) {
            // Fallback to method signature if no key expression provided
            return joinPoint.getSignature().toShortString();
        }

        try {
            // Create evaluation context with method parameters
            EvaluationContext context = createEvaluationContext(joinPoint);

            // Parse and evaluate the SpEL expression
            Expression expression = parser.parseExpression(keyExpression);
            String lockKey = expression.getValue(context, String.class);

            if (lockKey == null || lockKey.trim().isEmpty()) {
                throw new IllegalArgumentException("Lock key expression evaluated to null or empty");
            }

            return lockKey.trim();

        } catch (Exception e) {
            log.error("Error generating lock key from expression: {} - {}", keyExpression, e.getMessage());
            // Fallback to a safe default
            return "distributed-lock-" + Math.abs(keyExpression.hashCode());
        }
    }

    /**
     * Creates SpEL evaluation context with method parameters
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Get method signature and parameters
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // Add method parameters to context
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }

        // Add common variables
        context.setVariable("methodName", method.getName());
        context.setVariable("className", method.getDeclaringClass().getSimpleName());

        return context;
    }

    /**
     * Custom exception for distributed lock failures
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DistributedLockException extends BaseException {

        public DistributedLockException(String message) {
            super(message);
        }

        public DistributedLockException(String message, Throwable cause) {
            super(message, cause);
        }

        @Override
        public int getHttpStatusCode() {
            return HttpStatus.CONFLICT.value(); // 409 Conflict
        }
    }
}