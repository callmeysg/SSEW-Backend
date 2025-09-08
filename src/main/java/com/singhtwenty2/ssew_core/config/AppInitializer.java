package com.singhtwenty2.ssew_core.config;

import com.singhtwenty2.ssew_core.service.file_handeling.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer implements ApplicationRunner {

    private final S3Service s3Service;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing application...");
        try {
            s3Service.initializeS3Bucket();
            log.info("S3 storage initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize application: {}", e.getMessage(), e);
        }
    }
}
