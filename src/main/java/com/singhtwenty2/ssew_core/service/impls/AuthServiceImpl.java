package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.dto.LoginDTO.LoginRequest;
import com.singhtwenty2.ssew_core.data.dto.LoginDTO.LoginResponse;
import com.singhtwenty2.ssew_core.data.dto.RegisterDTO.RegisterRequest;
import com.singhtwenty2.ssew_core.data.dto.RegisterDTO.RegisterResponse;
import com.singhtwenty2.ssew_core.data.dto.common.UserMetadataDTO;
import com.singhtwenty2.ssew_core.data.entity.RefreshToken;
import com.singhtwenty2.ssew_core.data.entity.User;
import com.singhtwenty2.ssew_core.data.enums.UserRole;
import com.singhtwenty2.ssew_core.data.repository.RefreshTokenRepository;
import com.singhtwenty2.ssew_core.data.repository.UserRepository;
import com.singhtwenty2.ssew_core.security.EncoderService;
import com.singhtwenty2.ssew_core.service.AuthService;
import com.singhtwenty2.ssew_core.service.JwtService;
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
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.singhtwenty2.ssew_core.data.dto.TokenDTO.RefreshTokenRequest;

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
        log.debug("Starting user registration for phone: {}", registerRequest.getPhone());

        validateRegistrationRequest(registerRequest);
        checkExistingUser(registerRequest);

        User user = createUserFromRequest(registerRequest);
        User savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return RegisterResponse.builder()
                .success(true)
                .message("Registration successful. Please verify your email and phone number.")
                .userMetadata(buildUserMetadata(savedUser))
                .build();
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        log.debug("Login attempt for: {}", loginRequest.getPhone());

        validateLoginRequest(loginRequest);

        User user = authenticateUser(loginRequest);

        if (!user.canLogin()) {
            user.recordFailedLogin();
            userRepository.save(user);

            String reason = determineLoginFailureReason(user);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, reason);
        }

        cleanupUserSessions(user);

        String accessToken = jwtService.generateAccessToken(user.getId().toString());
        RefreshToken refreshToken = createRefreshToken(user);

        user.recordSuccessfulLogin();
        userRepository.save(user);

        log.info("User logged in successfully: {}", user.getId());

        return LoginResponse.builder()
                .success(true)
                .message("Login successful.")
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userMetadata(buildUserMetadata(user))
                .build();
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.debug("Token refresh attempt");

        if (!StringUtils.hasText(refreshTokenRequest.getRefreshTokenValue())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }

        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshTokenValue());

        if (tokenOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        RefreshToken refreshToken = tokenOptional.get();

        if (!refreshToken.isValid()) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();

        if (!user.canLogin()) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account access denied");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId().toString());

        refreshToken.setUpdatedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed successfully for user: {}", user.getId());

        return LoginResponse.builder()
                .success(true)
                .message("Token refreshed successfully")
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenRequest.getRefreshTokenValue())
                .tokenType("Bearer")
                .userMetadata(buildUserMetadata(user))
                .build();
    }

    @Override
    public void logout(RefreshTokenRequest refreshTokenRequest) {
        log.debug("Logout attempt");

        if (StringUtils.hasText(refreshTokenRequest.getRefreshTokenValue())) {
            Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshTokenValue());
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

    private void validateRegistrationRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getName()) || request.getName().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must be at least 2 characters long");
        }

        if (!StringUtils.hasText(request.getPhone()) || !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
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
        if (userRepository.findByPhoneNumber(request.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already registered");
        }

        if (StringUtils.hasText(request.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
    }

    private User createUserFromRequest(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName().trim());
        user.setPhoneNumber(request.getPhone());
        user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().toLowerCase() : null);
        user.setPassword(encoderService.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.USER);
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
        if (!StringUtils.hasText(request.getPhone()) || !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
    }

    private User authenticateUser(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByPhoneNumber(request.getPhone());

        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userOptional.get();

        if (!encoderService.matches(request.getPassword(), user.getPassword())) {
            user.recordFailedLogin();
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return user;
    }

    private String determineLoginFailureReason(User user) {
        if (!user.getIsActive()) {
            return "Account is deactivated";
        }
        if (user.getIsAccountLocked()) {
            return "Account is locked due to too many failed login attempts";
        }
        if (!user.getIsEmailVerified() && !user.getIsMobileVerified()) {
            return "Please verify your email or phone number before logging in";
        }
        return "Account access denied";
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
        String tokenValue = jwtService.generateRefreshToken(user.getId().toString());

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
                .phone(user.getPhoneNumber())
                .email(user.getEmail())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsMobileVerified())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lastLoginTime(user.getLastLoginTime() != null ? user.getLastLoginTime().toString() : null)
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString())
                .version(user.getVersion())
                .build();
    }

}
