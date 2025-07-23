package com.singhtwenty2.ssew_core.service;

import org.springframework.stereotype.Service;

@Service
public interface JwtService {
    public String generateToken(
            String userId,
            String type,
            Long expiry
    );

    public String generateAccessToken(
            String userId
    );

    public String generateRefreshToken(
            String userId
    );

    public boolean validateAccessToken(
            String token
    );

    public boolean validateRefreshToken(
            String token
    );

    public String getUserIdFromAccessToken(
            String token
    );
}
