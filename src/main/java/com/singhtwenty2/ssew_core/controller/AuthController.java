package com.singhtwenty2.ssew_core.controller;

import com.singhtwenty2.ssew_core.data.dto.AuthHealthResponse;
import com.singhtwenty2.ssew_core.data.dto.RegisterDTO.RegisterRequest;
import com.singhtwenty2.ssew_core.data.dto.RegisterDTO.RegisterResponse;
import com.singhtwenty2.ssew_core.service.AuthService;
import com.singhtwenty2.ssew_core.service.health.AuthHealthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.singhtwenty2.ssew_core.data.dto.LoginDTO.LoginRequest;
import static com.singhtwenty2.ssew_core.data.dto.LoginDTO.LoginResponse;
import static com.singhtwenty2.ssew_core.data.dto.TokenDTO.RefreshTokenRequest;
import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthHealthService authHealthService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Registration attempt from IP: {} for phone: {}",
                    getClientIP(request), registerRequest.getPhone());

            RegisterResponse response = authService.registerUser(registerRequest);

            log.info("User registration successful for phone: {}", registerRequest.getPhone());
            success = true;

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/register/", success, responseTime);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Login attempt from IP: {} for phone: {}",
                    getClientIP(request), loginRequest.getPhone());

            LoginResponse response = authService.loginUser(loginRequest);

            log.info("User login successful for phone: {}", loginRequest.getPhone());
            success = true;

            return ResponseEntity.ok(response);
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/login/", success, responseTime);
        }
    }

    @PostMapping("/refresh-tokens")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.debug("Token refresh attempt from IP: {}", getClientIP(request));

            LoginResponse response = authService.refreshToken(refreshTokenRequest);
            success = true;

            return ResponseEntity.ok(response);
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/rotate-tokens/", success, responseTime);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Logout attempt from IP: {}", getClientIP(request));

            authService.logout(refreshTokenRequest);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Logout successful"
            );
            success = true;

            return ResponseEntity.ok(response);
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/logout/", success, responseTime);
        }
    }

    @PostMapping("/logout-all-devices")
    public ResponseEntity<Map<String, Object>> logoutAllDevices(
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();

            log.info("Logout all devices attempt from IP: {} for user: {}", getClientIP(request), userId);

            authService.logoutAllDevices(userId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Logged out from all devices successfully"
            );
            success = true;

            return ResponseEntity.ok(response);
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("POST", "/v1/auth/logout-all/", success, responseTime);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<AuthHealthResponse> authServiceHealthCheck(
            HttpServletRequest request
    ) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Health check for auth service from IP: {}", getClientIP(request));

            AuthHealthResponse response = authHealthService.getDetailedHealthInfo();
            success = true;

            return ResponseEntity.ok(response);
        } finally {
            double responseTime = System.currentTimeMillis() - startTime;
            AuthHealthService.recordEndpointMetric("GET", "/v1/auth/health/", success, responseTime);
        }
    }
}