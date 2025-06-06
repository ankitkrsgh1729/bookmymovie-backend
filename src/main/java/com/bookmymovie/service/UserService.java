package com.bookmymovie.service;

import com.bookmymovie.constants.UserConstant;
import com.bookmymovie.entity.User;
import com.bookmymovie.event.UserRegistrationEvent;
import com.bookmymovie.exception.*;
import com.bookmymovie.repository.UserRepository;
import com.bookmymovie.security.JwtTokenProvider;
import com.bookmymovie.service.dto.AuthResponseDto;
import com.bookmymovie.service.dto.LoginRequestDto;
import com.bookmymovie.service.dto.UserRegistrationDto;
import com.bookmymovie.service.dto.UserResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private DatabaseLockService databaseLockService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Modify your registerUser method:
    @Transactional
    public UserResponseDto registerUserWithRole(UserRegistrationDto request, String ipAddress, UserConstant.UserRole role ) {

        // Step 1: Rate limiting checks
        rateLimitingService.checkIpRateLimit(ipAddress);
        rateLimitingService.checkEmailRateLimit(request.getEmail());

        // Step 2: Acquire database lock for this email
        if (!databaseLockService.acquireRegistrationLock(request.getEmail())) {
            throw new RegistrationInProgressException(
                    "Registration already in progress for this email. Please wait and try again.");
        }

        try {
            // Step 3: Check for existing user
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }

            // Step 4: Create and save user (your existing logic)
            User user = new User();
            user.setEmail(request.getEmail().toLowerCase().trim());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName().trim());
            user.setLastName(request.getLastName().trim());
            user.setPhoneNumber(request.getPhoneNumber().trim());
            user.setRole(role);
            user.setCreatedAt(LocalDateTime.now());
            user.setIpAddress(ipAddress);

            User savedUser = userRepository.save(user);

            // Step 5: Publish event for async processing
            UserRegistrationEvent event = new UserRegistrationEvent(
                    this, savedUser.getUserId(), savedUser.getEmail(),
                    savedUser.getFirstName(), savedUser.getLastName(), savedUser.getPhoneNumber(), savedUser.getIpAddress());
            eventPublisher.publishEvent(event);

            // Step 6: Reset email rate limit only on successful registration
            rateLimitingService.resetEmailRateLimit(request.getEmail());

            return UserResponseDto.fromEntity(savedUser);

        } catch (DataIntegrityViolationException e) {
            // Handle database constraint violations
            if (e.getMessage().contains("email")) {
                throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
            }
            throw new UserAlreadyExistsException("Registration failed due to data conflict");

        } finally {
            // Step 7: Always release the database lock
            databaseLockService.releaseRegistrationLock(request.getEmail());
        }
    }

    // User Authentication
    public AuthResponseDto authenticateUser(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new UserInactiveException("User account is not active");
        }

        String token = jwtTokenProvider.generateToken(user);

        return AuthResponseDto.builder()
                .token(token)
                .user(UserResponseDto.fromEntity(user))
                .expiresIn(jwtTokenProvider.getTokenExpiration())
                .build();
    }

    // Get user profile
    public UserResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserResponseDto.fromEntity(user);
    }
}
