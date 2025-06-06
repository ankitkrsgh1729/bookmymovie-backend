package com.bookmymovie.controller;

import com.bookmymovie.constants.UserConstant;
import com.bookmymovie.dto.response.ApiResponse;
import com.bookmymovie.security.UserPrincipal;
import com.bookmymovie.service.UserService;
import com.bookmymovie.service.dto.AuthResponseDto;
import com.bookmymovie.service.dto.LoginRequestDto;
import com.bookmymovie.service.dto.UserRegistrationDto;
import com.bookmymovie.service.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
            @Valid @RequestBody UserRegistrationDto request) {

        UserResponseDto user = userService.registerUserWithRole(request, request.getIpAddress(), UserConstant.UserRole.USER);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> loginUser(
            @Valid @RequestBody LoginRequestDto request) {

        AuthResponseDto authResponse = userService.authenticateUser(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse)
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        UserResponseDto user = userService.getUserProfile(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("User profile retrieved", user)
        );
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')") // Only existing admins can create new admins
    public ResponseEntity<ApiResponse<UserResponseDto>> registerAdmin(
            @Valid @RequestBody UserRegistrationDto request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        UserResponseDto user = userService.registerUserWithRole(request, ipAddress, UserConstant.UserRole.ADMIN);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin registered successfully", user));
    }


    private String getClientIpAddress(HttpServletRequest request) {
        // Check for IP address from various headers (for load balancers/proxies)
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Handle multiple IPs (X-Forwarded-For can contain multiple IPs)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}