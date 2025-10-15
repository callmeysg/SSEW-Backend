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
package com.singhtwenty2.commerce_service.service.backup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupService {

    private final S3Client s3Client;
    private final DataSource dataSource;

    @Value("${aws.s3.temp-bucket}")
    private String s3Bucket;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${backup.s3.folder:database-backups}")
    private String backupFolder;

    @Value("${backup.retention.count:3}")
    private int backupsToKeep;

    @Value("${backup.enabled:true}")
    private boolean backupEnabled;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Scheduled backup job - runs daily at 3:00 AM
     * Cron expression: second minute hour day month weekday
     */
    @Scheduled(cron = "${backup.schedule:0 0 3 * * *}")
    public void performScheduledBackup() {
        if (!backupEnabled) {
            log.info("Database backup is disabled. Skipping scheduled backup.");
            return;
        }

        log.info("=== Starting scheduled database backup at {} ===", LocalDateTime.now());

        try {
            executeBackup();
            log.info("=== Database backup completed successfully at {} ===", LocalDateTime.now());
        } catch (Exception e) {
            log.error("!!! CRITICAL: Database backup failed at {} !!!", LocalDateTime.now(), e);
            // Fail silently - don't throw exception to prevent application crash
        }
    }

    /**
     * Main backup execution method
     */
    private void executeBackup() throws Exception {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String backupFileName = String.format("backup_%s.sql", timestamp);
        String compressedFileName = backupFileName + ".gz";

        Path tempBackupFile = null;
        Path compressedFile = null;

        try {
            log.info("Step 1/5: Creating PostgreSQL backup using pg_dump");
            tempBackupFile = createDatabaseDump(backupFileName);
            log.info("Backup file created: {} (size: {} bytes)",
                    tempBackupFile.getFileName(), Files.size(tempBackupFile));

            log.info("Step 2/5: Compressing backup file");
            compressedFile = compressBackup(tempBackupFile, compressedFileName);
            log.info("Backup compressed: {} (size: {} bytes)",
                    compressedFile.getFileName(), Files.size(compressedFile));

            log.info("Step 3/5: Uploading backup to S3");
            uploadToS3(compressedFile, compressedFileName);
            log.info("Upload complete to s3://{}/{}/{}", s3Bucket, backupFolder, compressedFileName);

            log.info("Step 4/5: Rotating old backups (keeping latest {})", backupsToKeep);
            rotateOldBackups();
            log.info("Backup rotation complete");

            log.info("Step 5/5: Cleaning up temporary files");
            cleanupTempFiles(tempBackupFile, compressedFile);
            log.info("Cleanup complete");

        } catch (Exception e) {
            log.error("Backup process failed during execution", e);
            if (tempBackupFile != null) {
                cleanupTempFiles(tempBackupFile, compressedFile);
            }
            throw e;
        }
    }

    /**
     * Creates database dump using pg_dump command via Runtime.exec
     * This connects to the PostgreSQL container from within the commerce-service container
     */
    private Path createDatabaseDump(String filename) throws IOException, InterruptedException {
        Path tempFile = Files.createTempFile("db_backup_", ".sql");

        String jdbcUrl = extractJdbcUrl();
        String host = extractHost(jdbcUrl);
        String port = extractPort(jdbcUrl);
        String database = extractDatabase(jdbcUrl);

        log.info("Connecting to PostgreSQL at {}:{}/{}", host, port, database);

        String password = extractPassword();

        ProcessBuilder processBuilder = new ProcessBuilder(
                "pg_dump",
                "-h", host,
                "-p", port,
                "-U", dbUsername,
                "-d", database,
                "--format=plain",
                "--no-owner",
                "--no-acl"
        );

        processBuilder.environment().put("PGPASSWORD", password);
        processBuilder.redirectOutput(tempFile.toFile());
        processBuilder.redirectErrorStream(false);

        Path errorFile = Files.createTempFile("db_backup_error_", ".log");
        processBuilder.redirectError(errorFile.toFile());

        log.debug("Executing pg_dump command...");
        Process process = processBuilder.start();
        boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.MINUTES);

        if (!finished) {
            process.destroyForcibly();
            cleanupTempFiles(errorFile);
            throw new IOException("Database dump timed out after 10 minutes");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String errorOutput = Files.readString(errorFile);
            cleanupTempFiles(errorFile);
            throw new IOException("Database dump failed with exit code " + exitCode + ": " + errorOutput);
        }

        cleanupTempFiles(errorFile);

        return tempFile;
    }

    /**
     * Extract JDBC URL from datasource
     */
    private String extractJdbcUrl() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.getMetaData().getURL();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract JDBC URL", e);
        }
    }

    /**
     * Extract password from datasource connection
     */
    private String extractPassword() {
        try (Connection conn = dataSource.getConnection()) {
            return System.getenv("SPRING_DATASOURCE_PASSWORD");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract database password", e);
        }
    }

    /**
     * Extract host from JDBC URL
     */
    private String extractHost(String jdbcUrl) {
        String[] parts = jdbcUrl.split("//")[1].split("/")[0].split(":");
        return parts[0];
    }

    /**
     * Extract port from JDBC URL
     */
    private String extractPort(String jdbcUrl) {
        String hostPart = jdbcUrl.split("//")[1].split("/")[0];
        if (hostPart.contains(":")) {
            return hostPart.split(":")[1];
        }
        return "5432";
    }

    /**
     * Extract database name from JDBC URL
     */
    private String extractDatabase(String jdbcUrl) {
        String dbPart = jdbcUrl.split("//")[1].split("/")[1];
        if (dbPart.contains("?")) {
            dbPart = dbPart.split("\\?")[0];
        }
        return dbPart;
    }

    /**
     * Compresses the backup file using GZIP
     */
    private Path compressBackup(Path sourceFile, String compressedFileName) throws IOException {
        Path compressedFile = Files.createTempFile("db_backup_", ".sql.gz");

        try (InputStream fis = Files.newInputStream(sourceFile);
             OutputStream fos = Files.newOutputStream(compressedFile);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, len);
            }
        }

        return compressedFile;
    }

    /**
     * Uploads the compressed backup to S3
     */
    private void uploadToS3(Path file, String fileName) throws IOException {
        String s3Key = backupFolder + "/" + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Bucket)
                .key(s3Key)
                .contentType("application/gzip")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
    }

    /**
     * Rotates old backups in S3, keeping only the latest N backups
     */
    private void rotateOldBackups() {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(s3Bucket)
                    .prefix(backupFolder + "/backup_")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            List<S3Object> sortedObjects = listResponse.contents().stream()
                    .sorted(Comparator.comparing(S3Object::lastModified))
                    .collect(Collectors.toList());

            int totalBackups = sortedObjects.size();
            int backupsToDelete = totalBackups - backupsToKeep;

            if (backupsToDelete <= 0) {
                log.info("Total backups: {}. No old backups to delete.", totalBackups);
                return;
            }

            log.info("Total backups: {}. Deleting {} old backup(s).", totalBackups, backupsToDelete);

            for (int i = 0; i < backupsToDelete; i++) {
                S3Object objectToDelete = sortedObjects.get(i);
                String key = objectToDelete.key();

                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);
                log.info("Deleted old backup: {}", key);
            }

        } catch (Exception e) {
            log.error("Failed to rotate old backups in S3", e);
            // Don't throw - this is not critical enough to fail the entire backup
        }
    }

    /**
     * Cleans up temporary files
     */
    private void cleanupTempFiles(Path... files) {
        for (Path file : files) {
            if (file != null && Files.exists(file)) {
                try {
                    Files.delete(file);
                    log.debug("Deleted temporary file: {}", file.getFileName());
                } catch (IOException e) {
                    log.warn("Failed to delete temporary file: {}", file.getFileName(), e);
                }
            }
        }
    }

    /**
     * Manual backup trigger - can be called from a REST endpoint if needed
     */
    public void triggerManualBackup() {
        log.info("Manual backup triggered");
        performScheduledBackup();
    }
}