package com.singhtwenty2.ssew_core.service.auth;

import com.singhtwenty2.ssew_core.data.dto.auth.LoginDTO.LoginResponse;
import com.singhtwenty2.ssew_core.data.dto.auth.RegisterDTO.RegisterRequest;
import com.singhtwenty2.ssew_core.data.dto.auth.RegisterDTO.RegisterResponse;
import org.springframework.stereotype.Service;

import static com.singhtwenty2.ssew_core.data.dto.auth.LoginDTO.LoginRequest;
import static com.singhtwenty2.ssew_core.data.dto.auth.TokenDTO.RefreshTokenRequest;

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
