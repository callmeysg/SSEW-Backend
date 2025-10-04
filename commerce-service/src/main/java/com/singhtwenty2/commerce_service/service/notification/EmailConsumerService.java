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
package com.singhtwenty2.commerce_service.service.notification;

import com.singhtwenty2.commerce_service.data.dto.notification.EmailEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailConsumerService {

    private final RedisQueueService redisQueueService;
    private final EmailService emailService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;

    private static final int MAX_RETRIES = 3;
    private static final long POLL_INTERVAL_MS = 1000;

    @PostConstruct
    public void startConsuming() {
        if (running.compareAndSet(false, true)) {
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this::consumeEmailEvents);
            log.info("Email consumer service started");
        }
    }

    @PreDestroy
    public void stopConsuming() {
        running.set(false);
        if (executorService != null) {
            executorService.shutdown();
            log.info("Email consumer service stopped");
        }
    }

    private void consumeEmailEvents() {
        while (running.get()) {
            try {
                EmailEvent event = redisQueueService.consumeEmailEvent();

                if (event != null) {
                    processEmailEvent(event);
                } else {
                    Thread.sleep(POLL_INTERVAL_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Email consumer thread interrupted");
                break;
            } catch (Exception e) {
                log.error("Unexpected error in email consumer: {}", e.getMessage(), e);
                try {
                    Thread.sleep(POLL_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processEmailEvent(EmailEvent event) {
        try {
            if (event.getRetryCount() == null) {
                event.setRetryCount(0);
            }

            if (event.getEventId() == null) {
                event.setEventId(java.util.UUID.randomUUID().toString());
            }

            log.info("Processing email event: {} (Attempt #{}/{})",
                    event.getEventId(), event.getRetryCount() + 1, MAX_RETRIES);

            if ("NEW_ORDER".equals(event.getEventType())) {
                emailService.sendNewOrderNotification(event);
            } else {
                log.warn("Unknown email event type: {}", event.getEventType());
            }

            redisQueueService.markAsProcessed(event);
            log.info("Email event processed successfully: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process email event {}: {}", event.getEventId(), e.getMessage(), e);
            handleFailure(event);
        }
    }

    private void handleFailure(EmailEvent event) {
        if (event.getRetryCount() < MAX_RETRIES) {
            log.info("Scheduling retry for email event: {} (Retry #{}/{})",
                    event.getEventId(), event.getRetryCount() + 1, MAX_RETRIES);
            redisQueueService.requeueForRetry(event);
        } else {
            log.error("Max retries exceeded for email event: {}. Removing from queue.", event.getEventId());
            redisQueueService.removeFromQueue(event);
        }
    }
}