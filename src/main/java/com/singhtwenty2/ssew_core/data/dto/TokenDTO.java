package com.singhtwenty2.ssew_core.data.dto;

import lombok.*;

public class TokenDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshTokenValue;
    }
}
