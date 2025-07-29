package com.singhtwenty2.ssew_core.data.dto.auth;

import lombok.*;

public class TokenDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefreshTokenRequest {
        private String refresh_token_value;
    }
}
