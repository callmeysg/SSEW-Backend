package com.singhtwenty2.ssew_core.data.dto.auth;

import com.singhtwenty2.ssew_core.data.dto.auth.common.UserMetadataDTO;
import lombok.*;

public class RegisterDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        private String name;
        private String mobile_number;
        private String email;
        private String password;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterResponse {
        private String additional_notes;
        private UserMetadataDTO user_metadata;
    }
}
