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
package com.singhtwenty2.commerce_service.service.impls;

import com.singhtwenty2.commerce_service.service.aux.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @Override
    public void blacklistToken(String token, long expirationMs) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        long ttlSeconds = expirationMs / 1000;

        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofSeconds(ttlSeconds));
            log.debug("Token blacklisted with TTL: {} seconds", ttlSeconds);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void removeFromBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }
}
