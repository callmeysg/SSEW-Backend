package com.singhtwenty2.commerce_service.service.auth;

import com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.ProfileResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.UpdateProfileRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.TokenDTO.RotateTokenRequest;

public interface AuthService {

    RegisterResponse registerUser(RegisterRequest registerRequest);

    RegisterResponse registerAdmin(RegisterRequest registerRequest);

    LoginResponse loginUser(LoginRequest loginRequest);

    LoginResponse loginAdmin(LoginRequest loginRequest);

    LoginResponse rotateTokens(RotateTokenRequest rotateTokenRequest);

    void logout(RotateTokenRequest rotateTokenRequest);

    void logoutAllDevices(String userId);

    ProfileResponse getUserProfile(String userId);

    ProfileResponse updateUserProfile(String userId, UpdateProfileRequest updateRequest);
}