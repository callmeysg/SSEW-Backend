package com.singhtwenty2.ssew_core.data.dto;

import com.singhtwenty2.ssew_core.data.dto.common.UserMetadataDTO;
import com.singhtwenty2.ssew_core.data.enums.UserRole;
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
        private String phone;
        private String email;
        private String password;
        private UserRole role;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterResponse {
        private Boolean success;
        private String message;
        private UserMetadataDTO userMetadata;
    }
}
