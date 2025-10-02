/**
 * Copyright 2025 SSEW Core Service
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.ssew_core.service.polling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singhtwenty2.ssew_core.data.dto.polling.PoolingDTO.PollEvent;
import com.singhtwenty2.ssew_core.data.enums.PollActionType;
import com.singhtwenty2.ssew_core.data.enums.PollEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisEventService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String EVENT_KEY_PREFIX = "poll:events:";
    private static final String USER_EVENT_KEY_PREFIX = "poll:user:";
    private static final String ADMIN_EVENT_KEY = "poll:admin:events";
    private static final long DEFAULT_TTL_SECONDS = 300;
    private static final int MAX_EVENTS_PER_POLL = 50;

    public void publishOrderStatusChangeEvent(String orderId, String userId, String newStatus) {
        PollEvent event = PollEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(PollEventType.CUSTOMER_ORDER_STATUS)
                .action(PollActionType.REFRESH)
                .entityId(orderId)
                .entityType("ORDER")
                .metadata(Map.of(
                        "status", newStatus,
                        "orderId", orderId
                ))
                .timestamp(LocalDateTime.now())
                .ttl(DEFAULT_TTL_SECONDS)
                .build();

        String userKey = USER_EVENT_KEY_PREFIX + userId;
        saveEventToSortedSet(userKey, event);
    }

    public void publishNewOrderEventForAdmin(String orderId, String customerName, String totalAmount) {
        PollEvent event = PollEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(PollEventType.ADMIN_NEW_ORDER)
                .action(PollActionType.FETCH_NEW)
                .entityId(orderId)
                .entityType("ORDER")
                .metadata(Map.of(
                        "orderId", orderId,
                        "customerName", customerName,
                        "totalAmount", totalAmount,
                        "timestamp", LocalDateTime.now().toString()
                ))
                .timestamp(LocalDateTime.now())
                .ttl(DEFAULT_TTL_SECONDS)
                .build();

        saveEventToSortedSet(ADMIN_EVENT_KEY, event);
    }

    public void publishOrderUpdateEventForAdmin(String orderId, String updateType, Map<String, Object> details) {
        PollEvent event = PollEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(PollEventType.ADMIN_ORDER_UPDATE)
                .action(PollActionType.UPDATE_PARTIAL)
                .entityId(orderId)
                .entityType("ORDER")
                .metadata(new HashMap<>(details) {{
                    put("updateType", updateType);
                    put("orderId", orderId);
                }})
                .timestamp(LocalDateTime.now())
                .ttl(DEFAULT_TTL_SECONDS)
                .build();

        saveEventToSortedSet(ADMIN_EVENT_KEY, event);
    }

    public List<PollEvent> getUserEvents(String userId, String lastEventId) {
        String userKey = USER_EVENT_KEY_PREFIX + userId;
        return getEventsFromSortedSet(userKey, lastEventId);
    }

    public List<PollEvent> getAdminEvents(String lastEventId) {
        return getEventsFromSortedSet(ADMIN_EVENT_KEY, lastEventId);
    }

    public List<PollEvent> getEventsByType(PollEventType eventType, String userId, String lastEventId) {
        String key = eventType == PollEventType.CUSTOMER_ORDER_STATUS ?
                USER_EVENT_KEY_PREFIX + userId : ADMIN_EVENT_KEY;

        List<PollEvent> allEvents = getEventsFromSortedSet(key, lastEventId);
        return allEvents.stream()
                .filter(event -> event.getEventType() == eventType)
                .collect(Collectors.toList());
    }

    private void saveEventToSortedSet(String key, PollEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            double score = event.getTimestamp().toEpochSecond(ZoneOffset.UTC);

            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            zSetOps.add(key, eventJson, score);

            redisTemplate.expire(key, event.getTtl(), TimeUnit.SECONDS);

            Long size = zSetOps.size(key);
            if (size != null && size > MAX_EVENTS_PER_POLL * 2) {
                zSetOps.removeRange(key, 0, size - MAX_EVENTS_PER_POLL - 1);
            }

            log.debug("Event published to Redis: {}", event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage());
        }
    }

    private List<PollEvent> getEventsFromSortedSet(String key, String lastEventId) {
        List<PollEvent> events = new ArrayList<>();

        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            double minScore = 0;
            if (lastEventId != null) {
                PollEvent lastEvent = findEventById(key, lastEventId);
                if (lastEvent != null) {
                    minScore = lastEvent.getTimestamp().toEpochSecond(ZoneOffset.UTC) + 1;
                }
            }

            double maxScore = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

            Set<String> eventJsons = zSetOps.rangeByScore(key, minScore, maxScore, 0, MAX_EVENTS_PER_POLL);

            if (eventJsons != null) {
                for (String eventJson : eventJsons) {
                    try {
                        PollEvent event = objectMapper.readValue(eventJson, PollEvent.class);
                        events.add(event);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to deserialize event: {}", e.getMessage());
                    }
                }
            }

            cleanupExpiredEvents(key);

        } catch (Exception e) {
            log.error("Failed to retrieve events from Redis: {}", e.getMessage());
        }

        return events;
    }

    private PollEvent findEventById(String key, String eventId) {
        try {
            Set<String> allEvents = redisTemplate.opsForZSet().range(key, 0, -1);
            if (allEvents != null) {
                for (String eventJson : allEvents) {
                    PollEvent event = objectMapper.readValue(eventJson, PollEvent.class);
                    if (event.getEventId().equals(eventId)) {
                        return event;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to find event by ID: {}", e.getMessage());
        }
        return null;
    }

    private void cleanupExpiredEvents(String key) {
        try {
            double maxScore = LocalDateTime.now().minusSeconds(DEFAULT_TTL_SECONDS).toEpochSecond(ZoneOffset.UTC);
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, maxScore);
        } catch (Exception e) {
            log.error("Failed to cleanup expired events: {}", e.getMessage());
        }
    }
}