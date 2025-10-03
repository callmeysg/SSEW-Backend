package com.singhtwenty2.commerce_service.util.io;

import com.singhtwenty2.commerce_service.security.PrincipalUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@Slf4j
public class AuthenticationUtils {

    private AuthenticationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static PrincipalUser validateAuthentication(Authentication authentication, HttpServletRequest request, String operation) {
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalUser principalUser)) {
            log.warn("Unauthorized {} attempt from IP: {}", operation, getClientIP(request));
            return null;
        }
        return principalUser;
    }

    public static String extractUserId(Authentication authentication, HttpServletRequest request, String operation) {
        PrincipalUser principalUser = validateAuthentication(authentication, request, operation);
        return principalUser != null ? principalUser.getUserId().toString() : null;
    }
}
