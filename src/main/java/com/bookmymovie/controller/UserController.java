package com.bookmymovie.controller;

import com.bookmymovie.dto.response.ApiResponse;
import com.bookmymovie.security.UserPrincipal;
import com.bookmymovie.service.UserService;
import com.bookmymovie.service.dto.AuthResponseDto;
import com.bookmymovie.service.dto.LoginRequestDto;
import com.bookmymovie.service.dto.UserRegistrationDto;
import com.bookmymovie.service.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        UserResponseDto user = userService.registerUser(request, request.getIpAddress());

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
}