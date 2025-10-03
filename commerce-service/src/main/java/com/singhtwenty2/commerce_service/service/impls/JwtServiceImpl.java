package com.singhtwenty2.commerce_service.service.impls;

import com.singhtwenty2.commerce_service.data.enums.UserRole;
import com.singhtwenty2.commerce_service.service.aux.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static com.singhtwenty2.commerce_service.constants.AuthConstants.TokenType.ACCESS_TOKEN;
import static com.singhtwenty2.commerce_service.constants.AuthConstants.TokenType.REFRESH_TOKEN;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token-validity:900000}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity:2592000000}") long refreshTokenValidity) {

        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }

        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        if (decodedKey.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes) long");
        }

        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
        this.accessTokenValidityMs = accessTokenValidity;
        this.refreshTokenValidityMs = refreshTokenValidity;
    }

    @Override
    public String generateToken(String userId, String role, String type, Long expiry) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("User role cannot be null or empty");
        }

        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("Token type cannot be null or empty");
        }

        if (expiry == null || expiry <= 0) {
            throw new IllegalArgumentException("Expiry must be positive");
        }

        try {
            UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);

        return Jwts.builder()
                .subject(userId)
                .claim("type", type)
                .claim("role", role.toUpperCase())
                .claim("iat", now.getTime() / 1000)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String generateAccessToken(String userId, String role) {
        return generateToken(userId, role, ACCESS_TOKEN, accessTokenValidityMs);
    }

    @Override
    public String generateRefreshToken(String userId, String role) {
        return generateToken(userId, role, REFRESH_TOKEN, refreshTokenValidityMs);
    }

    @Override
    public boolean validateAccessToken(String token) {
        return validateTokenByType(token, ACCESS_TOKEN);
    }

    @Override
    public String getUserIdFromAccessToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }

        String tokenType = (String) claims.get("type");
        if (!ACCESS_TOKEN.equals(tokenType)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token type"
            );
        }

        return claims.getSubject();
    }

    @Override
    public UserRole getUserRoleFromAccessToken(String token) {
        Claims claims = parseAllClaims(token);
        if (claims == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired access token"
            );
        }

        String tokenType = (String) claims.get("type");
        if (!ACCESS_TOKEN.equals(tokenType)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token type - expected access token"
            );
        }

        String roleString = (String) claims.get("role");
        if (!StringUtils.hasText(roleString)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token missing role information"
            );
        }

        try {
            return UserRole.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid role in token: " + roleString
            );
        }
    }

    private boolean validateTokenByType(String token, String expectedType) {
        try {
            Claims claims = parseAllClaims(token);
            if (claims == null) {
                return false;
            }

            String tokenType = (String) claims.get("type");
            if (!expectedType.equals(tokenType)) {
                log.debug("Token type mismatch. Expected: {}, Found: {}", expectedType, tokenType);
                return false;
            }

            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                log.debug("Token is expired");
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token is expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseAllClaims(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (!StringUtils.hasText(rawToken)) {
            return null;
        }

        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.debug("JWT parsing failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error parsing JWT token", e);
            return null;
        }
    }
}