package com.singhtwenty2.ssew_core.data.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthHealthResponse {

    private String status;
    private String service_name;
    private String version;
    private LocalDateTime timestamp;
    private long uptime;
    private DatabaseHealth database;
    private SecurityHealth security;
    private EndpointHealth endpoints;
    private SystemHealth system;
    private ActiveSessions active_sessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseHealth {
        private String status;
        private long connection_pool_size;
        private long active_connections;
        private long idle_connections;
        private double response_time;
        private boolean can_connect;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityHealth {
        private String jwt_status;
        private boolean token_validation_working;
        private long token_cache_size;
        private boolean refresh_token_service_active;
        private String encryption_status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointHealth {
        private List<EndpointInfo> endpoints;
        private long total_requests;
        private long successful_requests;
        private long failed_requests;
        private double success_rate;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EndpointInfo {
            private String path;
            private String method;
            private String status;
            private long request_count;
            private double average_response_time;
            private long last_access_time;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealth {
        private MemoryInfo memory;
        private String jvm_version;
        private long thread_count;
        private String os_name;
        private String os_version;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MemoryInfo {
            private long total_memory;
            private long free_memory;
            private long used_memory;
            private long max_memory;
            private double memory_usage_percentage;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveSessions {
        private long total_active_sessions;
        private long total_registered_users;
        private long sessions_last_24_hours;
        private long registrations_last_24_hours;
        private Map<String, Long> sessions_by_device;
    }
}