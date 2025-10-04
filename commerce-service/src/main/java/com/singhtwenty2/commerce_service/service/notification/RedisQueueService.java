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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singhtwenty2.commerce_service.data.dto.notification.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisQueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String EMAIL_QUEUE = "email:queue";
    private static final String EMAIL_PROCESSING = "email:processing:";
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);

    public void publishEmailEvent(EmailEvent event) {
        try {
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            if (event.getRetryCount() == null) {
                event.setRetryCount(0);
            }

            log.debug("Publishing email event - eventId: {}, eventType: {}, recipient: {}, retryCount: {}",
                    event.getEventId(), event.getEventType(), event.getRecipientEmail(), event.getRetryCount());

            redisTemplate.opsForList().rightPush(EMAIL_QUEUE, event);
            log.info("Published email event to Redis queue: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish email event to Redis: {}", e.getMessage(), e);
        }
    }

    public EmailEvent consumeEmailEvent() {
        try {
            Object result = redisTemplate.opsForList().leftPop(EMAIL_QUEUE);
            if (result != null) {
                EmailEvent event;
                if (result instanceof EmailEvent) {
                    event = (EmailEvent) result;
                } else {
                    event = objectMapper.convertValue(result, EmailEvent.class);
                }

                log.debug("Consumed email event from Redis: eventId={}, eventType={}, recipientEmail={}",
                        event.getEventId(), event.getEventType(), event.getRecipientEmail());

                if (event.getEventType() == null) {
                    log.error("EventType is null for event: {}", event.getEventId());
                }

                markAsProcessing(event);
                return event;
            }
        } catch (Exception e) {
            log.error("Failed to consume email event from Redis: {}", e.getMessage(), e);
        }
        return null;
    }

    public void markAsProcessed(EmailEvent event) {
        try {
            redisTemplate.delete(EMAIL_PROCESSING + event.getEventId());
            log.debug("Marked email event as processed: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to mark email event as processed: {}", e.getMessage(), e);
        }
    }

    public void requeueForRetry(EmailEvent event) {
        try {
            event.setRetryCount(event.getRetryCount() + 1);
            redisTemplate.delete(EMAIL_PROCESSING + event.getEventId());

            long delay = calculateExponentialBackoff(event.getRetryCount());
            log.info("Requeuing email event {} for retry #{} with {}ms delay",
                    event.getEventId(), event.getRetryCount(), delay);

            if (delay > 0) {
                Thread.sleep(delay);
            }

            redisTemplate.opsForList().rightPush(EMAIL_QUEUE, event);
        } catch (Exception e) {
            log.error("Failed to requeue email event: {}", e.getMessage(), e);
        }
    }

    public void removeFromQueue(EmailEvent event) {
        try {
            redisTemplate.delete(EMAIL_PROCESSING + event.getEventId());
            log.info("Removed email event from queue after max retries: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to remove email event from queue: {}", e.getMessage(), e);
        }
    }

    private void markAsProcessing(EmailEvent event) {
        try {
            redisTemplate.opsForValue().set(
                    EMAIL_PROCESSING + event.getEventId(),
                    event,
                    PROCESSING_TIMEOUT
            );
        } catch (Exception e) {
            log.error("Failed to mark email event as processing: {}", e.getMessage(), e);
        }
    }

    private long calculateExponentialBackoff(int retryCount) {
        return (long) Math.pow(2, retryCount) * 1000;
    }
}