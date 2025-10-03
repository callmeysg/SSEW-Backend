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
package com.singhtwenty2.commerce_service.service.auth;

import com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterResponse;
import org.springframework.stereotype.Service;

import static com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginRequest;
import static com.singhtwenty2.commerce_service.data.dto.auth.TokenDTO.RefreshTokenRequest;

@Service
public interface AuthService {

    public RegisterResponse registerUser(
            RegisterRequest registerRequest
    );

    public RegisterResponse registerAdmin(
            RegisterRequest registerRequest
    );

    public LoginResponse login(
            LoginRequest loginRequest
    );

    public LoginResponse refreshToken(
            RefreshTokenRequest refreshTokenRequest
    );

    public void logout(
            RefreshTokenRequest refreshTokenRequest
    );

    public void logoutAllDevices(
            String userId
    );
}
