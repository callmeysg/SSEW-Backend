package com.singhtwenty2.commerce_service.data.dto.auth.common;

import lombok.*;

@Setter
@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMetadataDTO {
    private String userId;
    private String name;
    private String phone;
    private String email;
    private String role;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private String createdAt;
    private String updatedAt;
}