package com.singhtwenty2.commerce_service.service.impls;

import com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.LoginDTO.LoginResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.ActiveSessionDTO;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.ProfileResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.ProfileDTO.UpdateProfileRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterRequest;
import com.singhtwenty2.commerce_service.data.dto.auth.RegisterDTO.RegisterResponse;
import com.singhtwenty2.commerce_service.data.dto.auth.common.UserMetadataDTO;
import com.singhtwenty2.commerce_service.data.entity.RefreshToken;
import com.singhtwenty2.commerce_service.data.entity.User;
import com.singhtwenty2.commerce_service.data.enums.UserRole;
import com.singhtwenty2.commerce_service.data.repository.RefreshTokenRepository;
import com.singhtwenty2.commerce_service.data.repository.UserRepository;
import com.singhtwenty2.commerce_service.security.EncoderService;
import com.singhtwenty2.commerce_service.service.auth.AuthService;
import com.singhtwenty2.commerce_service.service.aux.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.singhtwenty2.commerce_service.data.dto.auth.TokenDTO.RotateTokenRequest;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final EncoderService encoderService;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[1-9]\\d{1,14}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    private static final int MAX_ACTIVE_SESSIONS = 5;

    @Override
    public RegisterResponse registerUser(RegisterRequest registerRequest) {
        log.debug("Starting user registration for phone: {}", registerRequest.getMobileNumber());

        validateRegistrationRequest(registerRequest);
        checkExistingUser(registerRequest);

        User user = createUserFromRequest(registerRequest, UserRole.USER);
        User savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return RegisterResponse.builder()
                .additionalNotes("You have been registered successfully. You can log in now.")
                .userMetadata(buildUserMetadata(savedUser))
                .build();
    }

    @Override
    public RegisterResponse registerAdmin(RegisterRequest registerRequest) {
        log.debug("Starting admin registration for phone: {}", registerRequest.getMobileNumber());

        validateRegistrationRequest(registerRequest);
        checkExistingUser(registerRequest);

        User user = createUserFromRequest(registerRequest, UserRole.ADMIN);
        User savedUser = userRepository.save(user);

        log.info("Admin registered successfully with ID: {}", savedUser.getId());

        return RegisterResponse.builder()
                .additionalNotes("You have been registered as an admin. You can log in now.")
                .userMetadata(buildUserMetadata(savedUser))
                .build();
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        log.debug("User login attempt for: {}", loginRequest.getMobileNumber());

        validateLoginRequest(loginRequest);
        User user = authenticateUser(loginRequest);

        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This endpoint is only for regular users");
        }

        if (!user.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        return performLogin(user);
    }

    @Override
    public LoginResponse loginAdmin(LoginRequest loginRequest) {
        log.debug("Admin login attempt for: {}", loginRequest.getMobileNumber());

        validateLoginRequest(loginRequest);
        User user = authenticateUser(loginRequest);

        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This endpoint is only for administrators");
        }

        if (!user.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        return performLogin(user);
    }

    @Override
    public LoginResponse rotateTokens(RotateTokenRequest rotateTokenRequest) {
        log.debug("Token rotation attempt");

        if (!StringUtils.hasText(rotateTokenRequest.getRefreshTokenValue())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }

        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(rotateTokenRequest.getRefreshTokenValue());

        if (tokenOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        RefreshToken oldRefreshToken = tokenOptional.get();

        if (!oldRefreshToken.isValid()) {
            oldRefreshToken.revoke();
            refreshTokenRepository.save(oldRefreshToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        User user = oldRefreshToken.getUser();

        if (!user.getIsActive()) {
            oldRefreshToken.revoke();
            refreshTokenRepository.save(oldRefreshToken);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        oldRefreshToken.revoke();
        refreshTokenRepository.save(oldRefreshToken);

        String newAccessToken = jwtService.generateAccessToken(user.getId().toString(), user.getRole().name());
        RefreshToken newRefreshToken = createRefreshToken(user);

        log.info("Tokens rotated successfully for user: {}", user.getId());

        return LoginResponse.builder()
                .tokenType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Override
    public void logout(RotateTokenRequest rotateTokenRequest) {
        log.debug("Logout attempt");

        if (StringUtils.hasText(rotateTokenRequest.getRefreshTokenValue())) {
            Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(rotateTokenRequest.getRefreshTokenValue());
            tokenOptional.ifPresent(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
                log.info("Refresh token revoked for user: {}", token.getUser().getId());
            });
        }
    }

    @Override
    public void logoutAllDevices(String userId) {
        log.debug("Logging out all devices for user: {}", userId);

        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
        if (userOptional.isPresent()) {
            refreshTokenRepository.revokeAllTokensForUser(userOptional.get());
            log.info("All tokens revoked for user: {}", userId);
        }
    }

    @Override
    public ProfileResponse getUserProfile(String userId) {
        log.debug("Fetching profile for user: {}", userId);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<RefreshToken> activeSessions = refreshTokenRepository.findActiveTokensForUser(user, LocalDateTime.now());

        List<ActiveSessionDTO> activeSessionDTOs = activeSessions.stream()
                .map(this::buildActiveSessionDTO)
                .collect(Collectors.toList());

        return ProfileResponse.builder()
                .userId(user.getId().toString())
                .name(user.getName())
                .phone(user.getMobileNumber())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsMobileVerified())
                .lastLoginTime(user.getLastLoginTime() != null ? user.getLastLoginTime().toString() : null)
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString())
                .activeSessions(activeSessionDTOs)
                .totalActiveSessions(activeSessionDTOs.size())
                .build();
    }

    @Override
    public ProfileResponse updateUserProfile(String userId, UpdateProfileRequest updateRequest) {
        log.debug("Updating profile for user: {}", userId);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean updated = false;

        if (StringUtils.hasText(updateRequest.getName()) && !updateRequest.getName().equals(user.getName())) {
            if (updateRequest.getName().trim().length() < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must be at least 2 characters long");
            }
            user.setName(updateRequest.getName().trim());
            updated = true;
        }

        if (StringUtils.hasText(updateRequest.getPhone()) && !updateRequest.getPhone().equals(user.getMobileNumber())) {
            if (!PHONE_PATTERN.matcher(updateRequest.getPhone()).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone number format");
            }
            if (userRepository.findByMobileNumber(updateRequest.getPhone()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already in use");
            }
            user.setMobileNumber(updateRequest.getPhone());
            user.setIsMobileVerified(false);
            updated = true;
        }

        if (StringUtils.hasText(updateRequest.getEmail()) && !updateRequest.getEmail().equals(user.getEmail())) {
            if (!EMAIL_PATTERN.matcher(updateRequest.getEmail()).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
            }
            if (userRepository.findByEmail(updateRequest.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
            }
            user.setEmail(updateRequest.getEmail().toLowerCase());
            user.setIsEmailVerified(false);
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Profile updated successfully for user: {}", userId);
        }

        return getUserProfile(userId);
    }

    private LoginResponse performLogin(User user) {
        cleanupUserSessions(user);

        String accessToken = jwtService.generateAccessToken(user.getId().toString(), user.getRole().name());
        RefreshToken refreshToken = createRefreshToken(user);

        user.recordSuccessfulLogin();
        userRepository.save(user);

        log.info("User logged in successfully: {}", user.getId());

        return LoginResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getName()) || request.getName().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must be at least 2 characters long");
        }

        if (!StringUtils.hasText(request.getMobileNumber()) || !PHONE_PATTERN.matcher(request.getMobileNumber()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone number format");
        }

        if (StringUtils.hasText(request.getEmail()) && !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        if (!StringUtils.hasText(request.getPassword()) || !PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters with uppercase, lowercase, number, and special character");
        }
    }

    private void checkExistingUser(RegisterRequest request) {
        if (userRepository.findByMobileNumber(request.getMobileNumber()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already registered");
        }

        if (StringUtils.hasText(request.getEmail()) &&
            userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
    }

    private User createUserFromRequest(RegisterRequest request, UserRole role) {
        User user = new User();
        user.setName(request.getName().trim());
        user.setMobileNumber(request.getMobileNumber());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().toLowerCase() : null);
        user.setPassword(encoderService.encode(request.getPassword()));
        user.setRole(role);
        user.setIsEmailVerified(false);
        user.setIsMobileVerified(false);
        user.setIsAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    private void validateLoginRequest(LoginRequest request) {
        if (!StringUtils.hasText(request.getMobileNumber()) || !PHONE_PATTERN.matcher(request.getMobileNumber()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
    }

    private User authenticateUser(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByMobileNumber(request.getMobileNumber());

        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userOptional.get();

        if (!encoderService.matches(request.getPassword(), user.getPassword())) {
            user.recordFailedLogin();
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        return user;
    }

    private void cleanupUserSessions(User user) {
        Long activeTokenCount = refreshTokenRepository.countActiveTokensForUser(user, LocalDateTime.now());

        if (activeTokenCount >= MAX_ACTIVE_SESSIONS) {
            log.debug("Max sessions reached for user: {}, cleaning up old tokens", user.getId());
            refreshTokenRepository.revokeAllTokensForUser(user);
        }

        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String extractDeviceInfo(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown Device";
        }

        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            return "Mobile Device";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    private RefreshToken createRefreshToken(User user) {
        String tokenValue = jwtService.generateRefreshToken(user.getId().toString(), user.getRole().name());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));
        refreshToken.setIsRevoked(false);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setUpdatedAt(LocalDateTime.now());

        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            refreshToken.setIpAddress(getClientIpAddress(request));
            refreshToken.setUserAgent(request.getHeader("User-Agent"));
            refreshToken.setDeviceInfo(extractDeviceInfo(request.getHeader("User-Agent")));
        }

        return refreshTokenRepository.save(refreshToken);
    }

    private UserMetadataDTO buildUserMetadata(User user) {
        return UserMetadataDTO.builder()
                .userId(user.getId().toString())
                .name(user.getName())
                .phone(user.getMobileNumber())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsMobileVerified())
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString())
                .build();
    }

    private ActiveSessionDTO buildActiveSessionDTO(RefreshToken token) {
        return ActiveSessionDTO.builder()
                .sessionId(token.getId().toString())
                .deviceInfo(token.getDeviceInfo())
                .ipAddress(token.getIpAddress())
                .userAgent(token.getUserAgent())
                .createdAt(token.getCreatedAt().toString())
                .expiresAt(token.getExpiresAt().toString())
                .lastUsedAt(token.getUpdatedAt().toString())
                .build();
    }
}