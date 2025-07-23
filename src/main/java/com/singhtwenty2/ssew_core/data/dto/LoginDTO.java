package com.singhtwenty2.ssew_core.data.dto;

import com.singhtwenty2.ssew_core.data.dto.common.UserMetadataDTO;
import lombok.*;

public class LoginDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String phone;
        private String password;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private Boolean success;
        private String message;
        private String tokenType;
        private String accessToken;
        private String refreshToken;
        private UserMetadataDTO userMetadata;
    }
}
