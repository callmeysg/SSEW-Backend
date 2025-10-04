package com.singhtwenty2.commerce_service.data.dto.auth;

import lombok.*;

public class TokenDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RotateTokenRequest {
        private String refreshTokenValue;
    }
}