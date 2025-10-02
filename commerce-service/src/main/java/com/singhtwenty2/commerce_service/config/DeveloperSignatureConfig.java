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
package com.singhtwenty2.commerce_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Base64;

@Configuration
public class DeveloperSignatureConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DeveloperSignatureInterceptor());
    }

    private static class DeveloperSignatureInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(
                @NonNull HttpServletRequest request,
                HttpServletResponse response,
                @NonNull Object handler) {
            response.setHeader("X-Dev-Name", "Aryan Singh (@singhtwenty2)");
            response.setHeader("X-Dev-Portfolio", "https://singhtwenty2.pages.dev/");
            response.setHeader("X-Dev-Alias", "singhtwenty2");

            String signature = createEncodedSignature();
            response.setHeader("X-Dev-Signature", signature);
            response.setHeader("X-Dev-Signature-Info", "Base64 encoded JSON");

            return true;
        }

        private String createEncodedSignature() {
            String message = """
                    {
                      "backend_developer": "Aryan Singh",
                      "x": "https://x.com/singhtwenty2",
                      "github": "https://github.com/singhtwenty2",
                      "portfolio": "https://singhtwenty2.pages.dev/",
                      "framework": "Spring Boot",
                      "message": "From draft to delivery"
                    }""";

            return Base64.getEncoder().encodeToString(message.getBytes());
        }
    }
}
