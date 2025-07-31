package com.singhtwenty2.ssew_core.data.dto.auth;

import com.singhtwenty2.ssew_core.data.dto.auth.common.UserMetadataDTO;
import lombok.*;

public class LoginDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String mobileNumber;
        private String password;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String additionalNotes;
        private String tokenType;
        private String accessToken;
        private String refreshToken;
        private UserMetadataDTO userMetadata;
    }
}
