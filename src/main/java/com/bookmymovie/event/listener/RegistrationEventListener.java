package com.bookmymovie.event.listener;

import com.bookmymovie.event.UserRegistrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegistrationEventListener {

    @EventListener
    @Order(1)
    public void logRegistration(UserRegistrationEvent event) {
        log.info("🎯 User registration initiated - ID: {}, Email: {}, IP: {}",
                event.getUserId(), event.getEmail(), event.getIpAddress());
    }

    @EventListener
    @Order(10)
    public void validateUserData(UserRegistrationEvent event) {
        log.info("🔍 Validating user data for user {} on thread: {}",
                event.getUserId(), Thread.currentThread().getName());

        try {
            // Mock validation processing
            Thread.sleep(100); // Simulate validation time
            log.info("✅ User data validation completed for user {}", event.getUserId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ User validation interrupted for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to validate user data for user {}: {}", event.getUserId(), e.getMessage());
        }
    }

    @EventListener
    @Order(20)
    @Async("emailExecutor")
    public void sendWelcomeEmail(UserRegistrationEvent event) {
        log.info("📧 Sending welcome email to user {} ({}) on thread: {}",
                event.getUserId(), event.getEmail(), Thread.currentThread().getName());

        try {
            // Mock email sending with realistic delay
            Thread.sleep(2000); // Simulate email service delay
            log.info("✅ Welcome email sent successfully to {} (User ID: {})",
                    event.getEmail(), event.getUserId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Welcome email sending interrupted for user {} ({})",
                    event.getUserId(), event.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to user {} ({}): {}",
                    event.getUserId(), event.getEmail(), e.getMessage());
        }
    }

    @EventListener
    @Order(30)
    @Async("analyticsExecutor")
    public void updateRegistrationAnalytics(UserRegistrationEvent event) {
        log.info("📊 Updating registration analytics for user {} on thread: {}",
                event.getUserId(), Thread.currentThread().getName());

        try {
            // Mock analytics processing
            Thread.sleep(1000); // Simulate analytics processing delay

            // Simulate different analytics operations
            log.debug("📈 Recording user registration metrics for {}", event.getEmail());
            log.debug("🎯 Updating regional statistics for IP: {}", event.getIpAddress());

            log.info("✅ Registration analytics updated for user {} ({})",
                    event.getUserId(), event.getEmail());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Analytics update interrupted for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to update analytics for user {}: {}", event.getUserId(), e.getMessage());
        }
    }

    @EventListener
    @Order(40)
    @Async("profileExecutor")
    public void createUserProfile(UserRegistrationEvent event) {
        log.info("👤 Creating user profile for user {} on thread: {}",
                event.getUserId(), Thread.currentThread().getName());

        try {
            // Mock profile creation
            Thread.sleep(1500); // Simulate profile creation delay

            log.debug("🏗️ Creating default user preferences for {}", event.getEmail());
            log.debug("🎬 Setting up movie preferences for user {}", event.getUserId());
            log.debug("📍 Configuring location settings for user {}", event.getUserId());

            log.info("✅ User profile created successfully for user {} ({})",
                    event.getUserId(), event.getEmail());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Profile creation interrupted for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to create profile for user {}: {}", event.getUserId(), e.getMessage());
        }
    }

    @EventListener
    @Order(50)
    @Async("notificationExecutor")
    public void sendSMSConfirmation(UserRegistrationEvent event) {
        log.info("📱 Sending SMS confirmation to user {} on thread: {}",
                event.getUserId(), Thread.currentThread().getName());

        try {
            // Mock SMS sending
            Thread.sleep(800); // Simulate SMS service delay

            log.debug("📞 Formatting SMS for phone: {}",
                    maskPhoneNumber(event.getPhoneNumber()));

            log.info("✅ SMS confirmation sent successfully to user {} ({})",
                    event.getUserId(), maskPhoneNumber(event.getPhoneNumber()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ SMS sending interrupted for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to send SMS to user {}: {}", event.getUserId(), e.getMessage());
        }
    }

    @EventListener
    @Order(60)
    @Async("marketingExecutor")
    public void setupMarketingPreferences(UserRegistrationEvent event) {
        log.info("🎯 Setting up marketing preferences for user {} on thread: {}",
                event.getUserId(), Thread.currentThread().getName());

        try {
            // Mock marketing setup
            Thread.sleep(600); // Simulate marketing processing delay

            log.debug("📈 Adding user {} to welcome campaign", event.getUserId());
            log.debug("🎬 Setting up movie recommendation engine for {}", event.getEmail());

            log.info("✅ Marketing preferences configured for user {} ({})",
                    event.getUserId(), event.getEmail());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Marketing setup interrupted for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to setup marketing for user {}: {}", event.getUserId(), e.getMessage());
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }
}