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
package com.singhtwenty2.commerce_service.controller.backup;

import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.service.backup.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/admin/backup")
@RequiredArgsConstructor
public class BackupController {

    private final DatabaseBackupService backupService;

    /**
     * Manual backup trigger endpoint
     * Only accessible by ADMIN users
     * Protected by developer secret header
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<String>> triggerBackup(
            @RequestHeader(value = "X-Developer-Secret", required = false) String developerSecret) {

        log.info("Manual backup triggered via API");

        new Thread(() -> {
            try {
                backupService.triggerManualBackup();
            } catch (Exception e) {
                log.error("Manual backup failed", e);
            }
        }).start();

        return ResponseEntity.ok(
                GlobalApiResponse.<String>builder()
                        .success(true)
                        .message("Database backup triggered successfully. Check logs for status.")
                        .data("Backup process started in background")
                        .build()
        );
    }
}