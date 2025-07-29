package com.singhtwenty2.ssew_core.security;

import com.singhtwenty2.ssew_core.data.enums.UserRole;
import lombok.*;

import java.util.UUID;

@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrincipalUser {
    private UUID userId;
    private UserRole role;
}

