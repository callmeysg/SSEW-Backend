/**
 * Copyright 2025 Aryan Singh
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.commerce_service.data.enums;

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