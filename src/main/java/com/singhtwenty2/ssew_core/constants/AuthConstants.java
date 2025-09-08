package com.singhtwenty2.ssew_core.constants;

public class AuthConstants {

    public static final class TokenType {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String RESET_TOKEN = "reset_token";

        private TokenType() {
        }
    }

    private AuthConstants() {
    }
}
