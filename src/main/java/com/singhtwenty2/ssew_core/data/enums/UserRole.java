package com.singhtwenty2.ssew_core.data.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum UserRole {
    USER(
            "User",
            Set.of(
                    "user:read-own",
                    "user:update-own",
                    "order:create",
                    "order:read-own",
                    "product:read"
            )
    ),
    ADMIN(
            "Administrator",
            Set.of(
                    "user:read-all",
                    "user:update-all",
                    "user:delete",
                    "product:create",
                    "product:update",
                    "product:delete",
                    "order:read-all",
                    "order:update-all",
                    "admin:dashboard"
            )
    );

    private final String displayName;
    private final Set<String> permissions;

    UserRole(String displayName, Set<String> permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}