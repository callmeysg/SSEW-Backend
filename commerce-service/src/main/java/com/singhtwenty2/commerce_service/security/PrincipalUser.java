package com.singhtwenty2.commerce_service.security;

import com.singhtwenty2.commerce_service.data.enums.UserRole;
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

