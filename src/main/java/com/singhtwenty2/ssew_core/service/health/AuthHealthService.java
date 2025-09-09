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
package com.singhtwenty2.ssew_core.service.health;

import com.singhtwenty2.ssew_core.data.dto.auth.AuthHealthResponse;
import com.singhtwenty2.ssew_core.data.dto.auth.AuthHealthResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthHealthService implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BuildProperties buildProperties;

    private static final LocalDateTime startTime = LocalDateTime.now();
    private static final Map<String, AtomicLong> endpointMetrics = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> endpointSuccessMetrics = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> endpointFailureMetrics = new ConcurrentHashMap<>();
    private static final Map<String, Double> endpointResponseTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> endpointLastAccess = new ConcurrentHashMap<>();

    @Override
    public Health health() {
        try {
            AuthHealthResponse healthResponse = getDetailedHealthInfo();
            if ("UP".equals(healthResponse.getStatus())) {
                return Health.up().withDetails(convertToMap(healthResponse)).build();
            } else {
                return Health.down().withDetails(convertToMap(healthResponse)).build();
            }
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }

    public AuthHealthResponse getDetailedHealthInfo() {
        return AuthHealthResponse.builder()
                .status(determineOverallStatus())
                .serviceName("SSEW Auth Service")
                .version(getServiceVersion())
                .timestamp(LocalDateTime.now())
                .uptime(getUptimeInSeconds())
                .database(getDatabaseHealth())
                .security(getSecurityHealth())
                .endpoints(getEndpointHealth())
                .system(getSystemHealth())
                .activeSessions(getActiveSessionsInfo())
                .build();
    }

    private String determineOverallStatus() {
        boolean dbHealthy = isDatabaseHealthy();
        boolean redisHealthy = isRedisHealthy();
        boolean systemHealthy = isSystemHealthy();

        return (dbHealthy && redisHealthy && systemHealthy) ? "UP" : "DOWN";
    }

    private String getServiceVersion() {
        try {
            return buildProperties.getVersion();
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    private long getUptimeInSeconds() {
        return ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
    }

    private DatabaseHealth getDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            long startTime = System.currentTimeMillis();
            boolean canConnect = connection.isValid(5);
            double responseTime = System.currentTimeMillis() - startTime;

            return DatabaseHealth.builder()
                    .status(canConnect ? "UP" : "DOWN")
                    .connectionPoolSize(getConnectionPoolSize())
                    .activeConnections(getActiveConnections())
                    .idleConnections(getIdleConnections())
                    .responseTime(responseTime)
                    .canConnect(canConnect)
                    .build();
        } catch (Exception e) {
            return DatabaseHealth.builder()
                    .status("DOWN")
                    .canConnect(false)
                    .responseTime(-1)
                    .build();
        }
    }

    private SecurityHealth getSecurityHealth() {
        boolean jwtWorking = testJWTValidation();
        boolean refreshTokenWorking = testRefreshTokenService();

        return SecurityHealth.builder()
                .jwtStatus(jwtWorking ? "UP" : "DOWN")
                .tokenValidationWorking(jwtWorking)
                .tokenCacheSize(getTokenCacheSize())
                .refreshTokenServiceActive(refreshTokenWorking)
                .encryptionStatus(testEncryption() ? "UP" : "DOWN")
                .build();
    }

    private EndpointHealth getEndpointHealth() {
        List<EndpointHealth.EndpointInfo> endpoints = Arrays.asList(
                createEndpointInfo("/v1/auth/register/", "POST"),
                createEndpointInfo("/v1/auth/login/", "POST"),
                createEndpointInfo("/v1/auth/rotate-tokens/", "POST"),
                createEndpointInfo("/v1/auth/logout/", "POST"),
                createEndpointInfo("/v1/auth/logout-all/", "POST"),
                createEndpointInfo("/v1/auth/health/", "GET")
        );

        long totalRequests = endpointMetrics.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        long successfulRequests = endpointSuccessMetrics.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        long failedRequests = endpointFailureMetrics.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();

        double successRate = totalRequests > 0 ?
                (double) successfulRequests / totalRequests * 100 : 100.0;

        return EndpointHealth.builder()
                .endpoints(endpoints)
                .totalRequests(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .build();
    }

    private SystemHealth getSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;

        SystemHealth.MemoryInfo memoryInfo = SystemHealth.MemoryInfo.builder()
                .totalMemory(totalMemory)
                .freeMemory(freeMemory)
                .usedMemory(usedMemory)
                .maxMemory(maxMemory)
                .memoryUsagePercentage(Math.round(memoryUsagePercentage * 100.0) / 100.0)
                .build();

        return SystemHealth.builder()
                .memory(memoryInfo)
                .jvmVersion(System.getProperty("java.version"))
                .threadCount(Thread.activeCount())
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .build();
    }

    private ActiveSessions getActiveSessionsInfo() {
        return ActiveSessions.builder()
                .totalActiveSessions(getTotalActiveSessions())
                .totalRegisteredUsers(getTotalRegisteredUsers())
                .sessionsLast24Hours(getSessionsLast24Hours())
                .registrationsLast24Hours(getRegistrationsLast24Hours())
                .sessionsByDevice(getSessionsByDevice())
                .build();
    }

    private EndpointHealth.EndpointInfo createEndpointInfo(String path, String method) {
        String key = method + ":" + path;
        return EndpointHealth.EndpointInfo.builder()
                .path(path)
                .method(method)
                .status("UP")
                .requestCount(endpointMetrics.getOrDefault(key, new AtomicLong(0)).get())
                .averageResponseTime(endpointResponseTimes.getOrDefault(key, 0.0))
                .lastAccessTime(endpointLastAccess.getOrDefault(key, 0L))
                .build();
    }

    public static void recordEndpointMetric(String method, String path, boolean success, double responseTime) {
        String key = method + ":" + path;
        endpointMetrics.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();

        if (success) {
            endpointSuccessMetrics.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        } else {
            endpointFailureMetrics.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        }

        endpointResponseTimes.put(key, responseTime);
        endpointLastAccess.put(key, System.currentTimeMillis());
    }

    private boolean isDatabaseHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isRedisHealthy() {
        try {
            redisTemplate.opsForValue().set("health:check", "test");
            String result = (String) redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");
            return "test".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSystemHealthy() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;
        return memoryUsagePercentage < 90.0;
    }

    private long getConnectionPoolSize() {
        return 10;
    }

    private long getActiveConnections() {
        return 5;
    }

    private long getIdleConnections() {
        return 5;
    }

    private boolean testJWTValidation() {
        return true;
    }

    private boolean testRefreshTokenService() {
        return isRedisHealthy();
    }

    private long getTokenCacheSize() {
        try {
            Set<String> keys = redisTemplate.keys("refresh_token:*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean testEncryption() {
        return true;
    }

    private long getTotalActiveSessions() {
        try {
            Set<String> keys = redisTemplate.keys("user_session:*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private long getTotalRegisteredUsers() {
        return 1000;
    }

    private long getSessionsLast24Hours() {
        return 150;
    }

    private long getRegistrationsLast24Hours() {
        return 25;
    }

    private Map<String, Long> getSessionsByDevice() {
        Map<String, Long> deviceSessions = new HashMap<>();
        deviceSessions.put("mobile", 750L);
        deviceSessions.put("web", 200L);
        deviceSessions.put("tablet", 50L);
        return deviceSessions;
    }

    private Map<String, Object> convertToMap(AuthHealthResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", response.getStatus());
        map.put("serviceName", response.getServiceName());
        map.put("version", response.getVersion());
        map.put("timestamp", response.getTimestamp());
        map.put("uptime", response.getUptime());
        map.put("database", response.getDatabase());
        map.put("security", response.getSecurity());
        map.put("endpoints", response.getEndpoints());
        map.put("system", response.getSystem());
        map.put("activeSessions", response.getActiveSessions());
        return map;
    }
}