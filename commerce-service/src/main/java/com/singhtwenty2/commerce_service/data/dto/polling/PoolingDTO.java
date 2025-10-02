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
package com.singhtwenty2.commerce_service.data.dto.polling;

import com.singhtwenty2.commerce_service.data.enums.PollActionType;
import com.singhtwenty2.commerce_service.data.enums.PollEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PoolingDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PollEvent {
        private String eventId;
        private PollEventType eventType;
        private PollActionType action;
        private String entityId;
        private String entityType;
        private Map<String, Object> metadata;
        private LocalDateTime timestamp;
        private Long ttl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PollResponse {
        private List<PollEvent> events;
        private String lastEventId;
        private Long pollInterval;
        private Boolean hasMore;
    }
}
