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
package com.singhtwenty2.ssew_core.controller.polling;

import com.singhtwenty2.ssew_core.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.ssew_core.data.dto.polling.PoolingDTO.PollEvent;
import com.singhtwenty2.ssew_core.data.dto.polling.PoolingDTO.PollResponse;
import com.singhtwenty2.ssew_core.data.enums.PollEventType;
import com.singhtwenty2.ssew_core.service.polling.RedisEventService;
import com.singhtwenty2.ssew_core.util.io.AuthenticationUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/polling")
@RequiredArgsConstructor
@Slf4j
public class PollingController {

    private final RedisEventService redisEventService;

    private static final long SHORT_POLL_INTERVAL_MS = 5000;
    private static final long LONG_POLL_INTERVAL_MS = 30000;

    @GetMapping("/events")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<PollResponse>> pollEvents(
            @RequestParam PollEventType eventType,
            @RequestParam(required = false) String lastEventId,
            @RequestParam(defaultValue = "false") boolean longPoll,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "poll events");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<PollResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        List<PollEvent> events = retrieveEvents(eventType, userId, lastEventId, isAdmin);

        if (longPoll && events.isEmpty()) {
            events = waitForEvents(eventType, userId, lastEventId, isAdmin);
        }

        PollResponse response = PollResponse.builder()
                .events(events)
                .lastEventId(events.isEmpty() ? lastEventId :
                        events.getLast().getEventId())
                .pollInterval(longPoll ? LONG_POLL_INTERVAL_MS : SHORT_POLL_INTERVAL_MS)
                .hasMore(events.size() >= 50)
                .build();

        return ResponseEntity.ok(
                GlobalApiResponse.<PollResponse>builder()
                        .success(true)
                        .message("Events retrieved")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/admin/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<PollResponse>> pollAdminEvents(
            @RequestParam(required = false) String lastEventId,
            @RequestParam(defaultValue = "false") boolean longPoll,
            HttpServletRequest request
    ) {
        log.debug("Admin polling events from IP: {}", request.getRemoteAddr());

        List<PollEvent> events = redisEventService.getAdminEvents(lastEventId);

        if (longPoll && events.isEmpty()) {
            events = waitForAdminEvents(lastEventId);
        }

        PollResponse response = PollResponse.builder()
                .events(events)
                .lastEventId(events.isEmpty() ? lastEventId :
                        events.getLast().getEventId())
                .pollInterval(longPoll ? LONG_POLL_INTERVAL_MS : SHORT_POLL_INTERVAL_MS)
                .hasMore(events.size() >= 50)
                .build();

        return ResponseEntity.ok(
                GlobalApiResponse.<PollResponse>builder()
                        .success(true)
                        .message("Admin events retrieved")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/user/events")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<PollResponse>> pollUserEvents(
            @RequestParam(required = false) String lastEventId,
            @RequestParam(defaultValue = "false") boolean longPoll,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "poll user events");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<PollResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        List<PollEvent> events = redisEventService.getUserEvents(userId, lastEventId);

        if (longPoll && events.isEmpty()) {
            events = waitForUserEvents(userId, lastEventId);
        }

        PollResponse response = PollResponse.builder()
                .events(events)
                .lastEventId(events.isEmpty() ? lastEventId :
                        events.getLast().getEventId())
                .pollInterval(longPoll ? LONG_POLL_INTERVAL_MS : SHORT_POLL_INTERVAL_MS)
                .hasMore(events.size() >= 50)
                .build();

        return ResponseEntity.ok(
                GlobalApiResponse.<PollResponse>builder()
                        .success(true)
                        .message("User events retrieved")
                        .data(response)
                        .build()
        );
    }

    private List<PollEvent> retrieveEvents(PollEventType eventType, String userId,
                                           String lastEventId, boolean isAdmin) {
        if (isAdmin && (eventType == PollEventType.ADMIN_NEW_ORDER ||
                        eventType == PollEventType.ADMIN_ORDER_UPDATE)) {
            return redisEventService.getAdminEvents(lastEventId);
        } else {
            return redisEventService.getEventsByType(eventType, userId, lastEventId);
        }
    }

    private List<PollEvent> waitForEvents(PollEventType eventType, String userId,
                                          String lastEventId, boolean isAdmin) {
        long startTime = System.currentTimeMillis();
        long timeout = 25000;

        while (System.currentTimeMillis() - startTime < timeout) {
            List<PollEvent> events = retrieveEvents(eventType, userId, lastEventId, isAdmin);
            if (!events.isEmpty()) {
                return events;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return List.of();
    }

    private List<PollEvent> waitForAdminEvents(String lastEventId) {
        long startTime = System.currentTimeMillis();
        long timeout = 25000;

        while (System.currentTimeMillis() - startTime < timeout) {
            List<PollEvent> events = redisEventService.getAdminEvents(lastEventId);
            if (!events.isEmpty()) {
                return events;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return List.of();
    }

    private List<PollEvent> waitForUserEvents(String userId, String lastEventId) {
        long startTime = System.currentTimeMillis();
        long timeout = 25000;

        while (System.currentTimeMillis() - startTime < timeout) {
            List<PollEvent> events = redisEventService.getUserEvents(userId, lastEventId);
            if (!events.isEmpty()) {
                return events;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return List.of();
    }
}