package com.singhtwenty2.ssew_core.data.dto.auth.common;

import lombok.*;

@Setter
@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMetadataDTO {
    private String user_id;
    private String name;
    private String phone;
    private String email;
    private String role;
    private Boolean is_email_verified;
    private Boolean is_phone_verified;
    private Integer failed_login_attempts;
    private String last_login_time;
    private String created_at;
    private String updated_at;
    private Long version;
}
