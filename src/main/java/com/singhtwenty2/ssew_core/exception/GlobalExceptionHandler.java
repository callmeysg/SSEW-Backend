package com.singhtwenty2.ssew_core.exception;

import com.singhtwenty2.ssew_core.data.dto.common.GlobalApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleNullPointerException(
            NullPointerException ex,
            HttpServletRequest request) {

        if (ex.getMessage() != null &&
            ex.getMessage().contains("authentication") &&
            ex.getMessage().contains("getPrincipal")) {

            log.warn("Authentication required for endpoint: {} from IP: {}",
                    request.getRequestURI(), getClientIP(request));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.builder()
                            .success(false)
                            .message("Authentication required")
                            .data(null)
                            .build()
            );
        }

        log.error("Unexpected null pointer exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("Internal server error")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("Authentication failed for endpoint: {} from IP: {}",
                request.getRequestURI(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("Authentication failed")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied for endpoint: {} from IP: {}. Reason: {}",
                request.getRequestURI(), getClientIP(request), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("You do not have permission to access this resource")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        log.warn("404 Not Found - Endpoint not found: {} {} from IP: {}",
                ex.getHttpMethod(), ex.getRequestURL(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("The requested resource was not found")
                        .data(null)
                        .build()
        );
    }
}