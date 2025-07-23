package com.singhtwenty2.ssew_core.data.dto.common;

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
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private Integer failedLoginAttempts;
    private String lastLoginTime;
    private String createdAt;
    private String updatedAt;
    private Long version;
}
