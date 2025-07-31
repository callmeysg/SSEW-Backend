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
    private String serviceName;
    private String version;
    private LocalDateTime timestamp;
    private long uptime;
    private DatabaseHealth database;
    private SecurityHealth security;
    private EndpointHealth endpoints;
    private SystemHealth system;
    private ActiveSessions activeSessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseHealth {
        private String status;
        private long connectionPoolSize;
        private long activeConnections;
        private long idleConnections;
        private double responseTime;
        private boolean canConnect;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityHealth {
        private String jwtStatus;
        private boolean tokenValidationWorking;
        private long tokenCacheSize;
        private boolean refreshTokenServiceActive;
        private String encryptionStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointHealth {
        private List<EndpointInfo> endpoints;
        private long totalRequests;
        private long successfulRequests;
        private long failedRequests;
        private double successRate;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EndpointInfo {
            private String path;
            private String method;
            private String status;
            private long requestCount;
            private double averageResponseTime;
            private long lastAccessTime;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealth {
        private MemoryInfo memory;
        private String jvmVersion;
        private long threadCount;
        private String osName;
        private String osVersion;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MemoryInfo {
            private long totalMemory;
            private long freeMemory;
            private long usedMemory;
            private long maxMemory;
            private double memoryUsagePercentage;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveSessions {
        private long totalActiveSessions;
        private long totalRegisteredUsers;
        private long sessionsLast24Hours;
        private long registrationsLast24Hours;
        private Map<String, Long> sessionsByDevice;
    }
}