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
package com.singhtwenty2.commerce_service.interceptor;

import com.singhtwenty2.commerce_service.annotation.ApiDeprecated;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@Component
@Slf4j
public class DeprecationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            ApiDeprecated deprecated = handlerMethod.getMethodAnnotation(ApiDeprecated.class);

            if (deprecated != null) {
                log.warn("DEPRECATED ENDPOINT ACCESSED: {} {} by IP: {}",
                        request.getMethod(), request.getRequestURI(), getClientIP(request));

                response.setHeader("X-API-Deprecated", "true");
                if (!deprecated.since().isEmpty()) {
                    response.setHeader("X-API-Deprecated-Date", deprecated.since());
                }
                if (!deprecated.replacement().isEmpty()) {
                    response.setHeader("X-API-Replacement", deprecated.replacement());
                }
                if (!deprecated.sunsetDate().isEmpty()) {
                    response.setHeader("X-API-Sunset-Date", deprecated.sunsetDate());
                }
                response.setHeader("X-API-Deprecation-Warning", deprecated.message());
                response.setHeader("Warning", "299 - \"" + deprecated.message() + "\"");
            }
        }
        return true;
    }
}