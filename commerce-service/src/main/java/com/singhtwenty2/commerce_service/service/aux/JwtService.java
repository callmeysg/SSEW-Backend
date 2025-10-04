package com.singhtwenty2.commerce_service.service.aux;

import com.singhtwenty2.commerce_service.data.enums.UserRole;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
    public String generateToken(
            String userId,
            String role,
            String type,
            Long expiry
    );

    public String generateAccessToken(
            String userId,
            String role
    );

    public String generateRefreshToken(
            String userId,
            String role
    );

    public boolean validateAccessToken(
            String token
    );

    public String getUserIdFromAccessToken(
            String token
    );

    public UserRole getUserRoleFromAccessToken(String token);
}
