package com.singhtwenty2.ssew_core.service;

import com.singhtwenty2.ssew_core.data.dto.LoginDTO.LoginResponse;
import com.singhtwenty2.ssew_core.data.dto.RegisterDTO.RegisterRequest;
import com.singhtwenty2.ssew_core.data.dto.RegisterDTO.RegisterResponse;
import org.springframework.stereotype.Service;

import static com.singhtwenty2.ssew_core.data.dto.LoginDTO.LoginRequest;
import static com.singhtwenty2.ssew_core.data.dto.TokenDTO.RefreshTokenRequest;

@Service
public interface AuthService {

    public RegisterResponse registerUser(
            RegisterRequest registerRequest
    );

    public LoginResponse loginUser(
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
