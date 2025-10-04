package com.singhtwenty2.commerce_service.exception;

import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        log.warn("Response status exception: {} {} from IP: {}",
                ex.getStatusCode(), ex.getReason(), getClientIP(request));

        return ResponseEntity.status(ex.getStatusCode()).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message(ex.getReason() != null ? ex.getReason() : "Request failed")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Business rule violation: {} from IP: {}", ex.getMessage(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} from IP: {}", ex.getMessage(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        log.warn("Duplicate resource: {} from IP: {}", ex.getMessage(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleInvalidOperationException(
            InvalidOperationException ex,
            HttpServletRequest request) {

        log.warn("Invalid operation: {} from IP: {}", ex.getMessage(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {} from IP: {}", errors, getClientIP(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.error("Data integrity violation from IP: {}", getClientIP(request), ex);

        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("Duplicate entry")) {
                message = "Resource already exists";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Referenced resource does not exist";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message(message)
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        log.warn("File upload size exceeded from IP: {}", getClientIP(request));

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("File size exceeds the maximum allowed limit")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument: {} from IP: {}", ex.getMessage(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.error("Illegal state: {} from IP: {}", ex.getMessage(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("Internal server error")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleUnsupportedOperationException(
            UnsupportedOperationException ex,
            HttpServletRequest request) {

        log.warn("Unsupported operation: {} from IP: {}", ex.getMessage(), getClientIP(request));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("Operation not supported")
                        .data(null)
                        .build()
        );
    }

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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.error("Unexpected runtime exception from IP: {}", getClientIP(request), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("An unexpected error occurred")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected exception from IP: {}", getClientIP(request), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GlobalApiResponse.builder()
                        .success(false)
                        .message("Internal server error")
                        .data(null)
                        .build()
        );
    }
}