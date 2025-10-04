/**
 * Copyright 2025 Aryan Singh
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.commerce_service.controller.auth;

import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.ProfileResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.UpdateProfileRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterResponse;
import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.security.PrincipalUser;
import com.singhtwenty2.commerce_service.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

import static com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginRequest;
import static com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginResponse;
import static com.singhtwenty2.commerce_service.data.dto.auth.TokenDTO.RotateTokenRequest;
import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Value("${app.security.developer-secret:}")
    private String developerSecret;

    @PostMapping("/register")
    public ResponseEntity<GlobalApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request
    ) {
        log.info("User Registration attempt from IP: {} for phone: {}",
                getClientIP(request), registerRequest.getMobileNumber());

        RegisterResponse response = authService.registerUser(registerRequest);

        log.info("User registration successful for phone: {}", registerRequest.getMobileNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<RegisterResponse>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/register-admin")
    public ResponseEntity<GlobalApiResponse<RegisterResponse>> registerAdmin(
            @Valid @RequestBody RegisterRequest registerRequest,
            @RequestHeader(value = "X-FA8S-Secret") String encodedSecret,
            HttpServletRequest request
    ) {
        if (encodedSecret == null || encodedSecret.trim().isEmpty()) {
            log.warn("Admin registration attempt without secret from IP: {}", getClientIP(request));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            GlobalApiResponse.<RegisterResponse>builder()
                                    .success(false)
                                    .message("Secret header is required for admin registration.")
                                    .data(null)
                                    .build()
                    );
        }

        String decodedSecret;
        try {
            decodedSecret = new String(Base64.getDecoder().decode(encodedSecret));
        } catch (IllegalArgumentException e) {
            log.warn("Admin registration attempt with invalid base64 secret from IP: {}", getClientIP(request));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            GlobalApiResponse.<RegisterResponse>builder()
                                    .success(false)
                                    .message("Invalid secret format. Please provide a valid secret.")
                                    .data(null)
                                    .build()
                    );
        }

        if (!developerSecret.equals(decodedSecret)) {
            log.warn("Admin registration attempt with wrong secret from IP: {} for phone: {}",
                    getClientIP(request), registerRequest.getMobileNumber());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            GlobalApiResponse.<RegisterResponse>builder()
                                    .success(false)
                                    .message("Invalid secret provided for admin registration.")
                                    .data(null)
                                    .build()
                    );
        }

        log.info("Admin registration attempt from IP: {} for phone: {}",
                getClientIP(request), registerRequest.getMobileNumber());

        RegisterResponse response = authService.registerAdmin(registerRequest);

        log.info("Admin registration successful for phone: {}", registerRequest.getMobileNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<RegisterResponse>builder()
                        .success(true)
                        .message("Admin registered successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        log.info("User login attempt from IP: {} for phone: {}",
                getClientIP(request), loginRequest.getMobileNumber());

        LoginResponse response = authService.loginUser(loginRequest);

        log.info("User login successful for phone: {}", loginRequest.getMobileNumber());

        return ResponseEntity.ok(
                GlobalApiResponse.<LoginResponse>builder()
                        .success(true)
                        .message("User logged in successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/login-admin")
    public ResponseEntity<GlobalApiResponse<LoginResponse>> loginAdmin(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        log.info("Admin login attempt from IP: {} for phone: {}",
                getClientIP(request), loginRequest.getMobileNumber());

        LoginResponse response = authService.loginAdmin(loginRequest);

        log.info("Admin login successful for phone: {}", loginRequest.getMobileNumber());

        return ResponseEntity.ok(
                GlobalApiResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Admin logged in successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/rotate-tokens")
    public ResponseEntity<GlobalApiResponse<LoginResponse>> rotateTokens(
            @Valid @RequestBody RotateTokenRequest rotateTokenRequest,
            HttpServletRequest request) {
        log.debug("Token rotation attempt from IP: {}", getClientIP(request));

        LoginResponse response = authService.rotateTokens(rotateTokenRequest);

        return ResponseEntity.ok(
                GlobalApiResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Tokens rotated successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> logout(
            @Valid @RequestBody RotateTokenRequest rotateTokenRequest,
            HttpServletRequest request) {
        log.info("Logout attempt from IP: {}", getClientIP(request));

        authService.logout(rotateTokenRequest);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("User logged out successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> logoutAllDevices(
            HttpServletRequest request,
            Authentication authentication
    ) {
        PrincipalUser user = (PrincipalUser) authentication.getPrincipal();
        String userId = user.getUserId().toString();

        log.info("Logout all devices attempt from IP: {} for user: {}", getClientIP(request), userId);

        authService.logoutAllDevices(userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("User logged out from all devices successfully")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<GlobalApiResponse<ProfileResponse>> getProfile(
            Authentication authentication,
            HttpServletRequest request
    ) {
        PrincipalUser user = (PrincipalUser) authentication.getPrincipal();
        String userId = user.getUserId().toString();

        log.info("Profile fetch request from IP: {} for user: {}", getClientIP(request), userId);

        ProfileResponse response = authService.getUserProfile(userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProfileResponse>builder()
                        .success(true)
                        .message("Profile fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/profile/update")
    public ResponseEntity<GlobalApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest,
            Authentication authentication,
            HttpServletRequest request
    ) {
        PrincipalUser user = (PrincipalUser) authentication.getPrincipal();
        String userId = user.getUserId().toString();

        log.info("Profile update request from IP: {} for user: {}", getClientIP(request), userId);

        ProfileResponse response = authService.updateUserProfile(userId, updateRequest);

        log.info("Profile updated successfully for user: {}", userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProfileResponse>builder()
                        .success(true)
                        .message("Profile updated successfully")
                        .data(response)
                        .build()
        );
    }
}