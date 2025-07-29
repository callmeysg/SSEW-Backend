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
        private String mobile_number;
        private String password;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String additional_notes;
        private String token_type;
        private String access_token;
        private String refresh_token;
        private UserMetadataDTO user_metadata;
    }
}
