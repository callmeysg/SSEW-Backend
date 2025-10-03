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

import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.AuthHealthResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterResponse;
import com.singhtwenty2.commerce_service.security.PrincipalUser;
import com.singhtwenty2.commerce_service.service.auth.AuthService;
import com.singhtwenty2.commerce_service.service.health.AuthHealthService;
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
import static com.singhtwenty2.commerce_service.data.dto.auth.TokenDTO.RefreshTokenRequest;
import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthHealthService authHealthService;

    @Value("${app.security.developer-secret:}")
    private String developerSecret;

    @PostMapping("/register")
    public ResponseEntity<GlobalApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("User Registration attempt from IP: {} for phone: {}",
                    getClientIP(request), registerRequest.getMobileNumber());

            RegisterResponse response = authService.registerUser(registerRequest);

            log.info("User registration successful for phone: {}", registerRequest.getMobileNumber());
            success = true;

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    GlobalApiResponse.<RegisterResponse>builder()
                            .success(true)
                            .message("User registered successfully")
                            .data(response)
                            .build()
            );
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/register", success, responseTime);
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<GlobalApiResponse<RegisterResponse>> registerAdmin(
            @Valid @RequestBody RegisterRequest registerRequest,
            @RequestHeader(value = "X-FA8S-Secret") String encodedSecret,
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
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
            success = true;

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    GlobalApiResponse.<RegisterResponse>builder()
                            .success(true)
                            .message("Admin registered successfully")
                            .data(response)
                            .build()
            );
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/register-admin", success, responseTime);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Login attempt from IP: {} for phone: {}",
                    getClientIP(request), loginRequest.getMobileNumber());

            LoginResponse response = authService.login(loginRequest);

            log.info("User login successful for phone: {}", loginRequest.getMobileNumber());
            success = true;

            return ResponseEntity.ok(
                    GlobalApiResponse.<LoginResponse>builder()
                            .success(true)
                            .message("User logged in successfully")
                            .data(response)
                            .build()
            );
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/login", success, responseTime);
        }
    }

    @PostMapping("/refresh-tokens")
    public ResponseEntity<GlobalApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.debug("Token refresh attempt from IP: {}", getClientIP(request));

            LoginResponse response = authService.refreshToken(refreshTokenRequest);
            success = true;

            return ResponseEntity.ok(
                    GlobalApiResponse.<LoginResponse>builder()
                            .success(true)
                            .message("Tokens refreshed successfully")
                            .data(response)
                            .build()
            );
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/rotate-tokens", success, responseTime);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> logout(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Logout attempt from IP: {}", getClientIP(request));

            authService.logout(refreshTokenRequest);

            success = true;

            return ResponseEntity.ok(
                    GlobalApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("User logged out successfully")
                            .data(null)
                            .build()
            );
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/logout", success, responseTime);
        }
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> logoutAllDevices(
            HttpServletRequest request,
            Authentication authentication
    ) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            PrincipalUser user = (PrincipalUser) authentication.getPrincipal();
            String userId = user.getUserId().toString();

            log.info("Logout all devices attempt from IP: {} for user: {}", getClientIP(request), userId);

            authService.logoutAllDevices(userId);

            success = true;

            return ResponseEntity.ok(
                    GlobalApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .message("User logged out from all devices successfully")
                            .data(null)
                            .build()
            );
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/logout-all", success, responseTime);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<GlobalApiResponse<AuthHealthResponse>> authServiceHealthCheck(
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Health check for auth service from IP: {}", getClientIP(request));

            AuthHealthResponse response = authHealthService.getDetailedHealthInfo();
            success = true;

            return ResponseEntity.ok(
                    GlobalApiResponse.<AuthHealthResponse>builder()
                            .success(true)
                            .message("Auth service health check successful")
                            .data(response)
                            .build()
            );
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("GET", "/v1/auth/health", success, responseTime);
        }
    }
}