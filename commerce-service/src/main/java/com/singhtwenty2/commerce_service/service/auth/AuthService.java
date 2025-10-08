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

import com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.ProfileResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.UpdateProfileRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterResponse;

import static com.singhtwenty2.commerce_service.data.dto.auth.TokenDTO.RotateTokenRequest;

public interface AuthService {

    RegisterResponse registerUser(RegisterRequest registerRequest);

    RegisterResponse registerAdmin(RegisterRequest registerRequest);

    LoginResponse loginUser(LoginRequest loginRequest);

    LoginResponse loginAdmin(LoginRequest loginRequest);

    LoginResponse rotateTokens(RotateTokenRequest rotateTokenRequest);

    void logout(RotateTokenRequest rotateTokenRequest, String accessToken);

    void logoutAllDevices(String userId, String currentAccessToken);

    ProfileResponse getUserProfile(String userId);

    ProfileResponse updateUserProfile(String userId, UpdateProfileRequest updateRequest);

    void changePassword(String userId, String currentPassword, String newPassword);
}