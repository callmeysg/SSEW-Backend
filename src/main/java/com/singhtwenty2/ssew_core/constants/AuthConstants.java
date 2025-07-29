package com.singhtwenty2.ssew_core.constants;

public class AuthConstants {

    public static final class Role {
        public static final String ADMIN = "ADMIN";
        public static final String USER = "USER";

        private Role() {
        }
    }

    public static final class OTPPurpose {
        public static final String REGISTRATION = "registration";
        public static final String LOGIN = "login";
        public static final String PASSWORD_RESET = "password_reset";
        public static final String EMAIL_VERIFICATION = "email_verification";
        public static final String MOBILE_VERIFICATION = "mobile_verification";
        public static final String ACCOUNT_UNLOCK = "account_unlock";
        public static final String TWO_FACTOR_AUTH = "two_factor_auth";

        private OTPPurpose() {
        }
    }

    public static final class TokenType {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String RESET_TOKEN = "reset_token";

        private TokenType() {
        }
    }

    public static final class RateLimitType {
        public static final String LOGIN_ATTEMPTS = "login_attempts";
        public static final String OTP_REQUESTS = "otp_requests";
        public static final String PASSWORD_RESET = "password_reset";
        public static final String REGISTRATION = "registration";

        private RateLimitType() {
        }
    }

    public static final class SecurityLimits {
        public static final int MAX_LOGIN_ATTEMPTS = 5;
        public static final int MAX_OTP_ATTEMPTS = 3;
        public static final int MAX_OTP_REQUESTS_PER_HOUR = 5;
        public static final int MAX_PASSWORD_RESET_ATTEMPTS = 3;
        public static final int OTP_LENGTH = 6;
        public static final int REFRESH_TOKEN_LENGTH = 64;

        private SecurityLimits() {
        }
    }

    private AuthConstants() {
    }
}
