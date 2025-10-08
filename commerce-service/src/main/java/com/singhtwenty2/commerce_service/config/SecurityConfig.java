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

import com.singhtwenty2.commerce_service.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/auth/register",      // Register endpoint
                                "/v1/auth/register-admin",// Register endpoint for ADMIN
                                "/v1/auth/login",         // Login endpoint
                                "/v1/auth/login-admin",   // Login endpoint for ADMIN
                                "/v1/auth/rotate-tokens", // Rotate tokens endpoint
                                "/v1/images/requirements/**",// Image requirement endpoints
                                "/v1/public/**",         // Public API endpoints
                                "/v1/health",            // Health check
                                "/actuator/health",      // Spring Boot health actuator
                                "/actuator/info",        // Application info
                                "/api/docs/**",          // API documentation
                                "/swagger-ui/**",        // Swagger UI
                                "/v3/api-docs/**",       // OpenAPI docs
                                "/favicon.ico",          // Favicon
                                "/v1/ping",              // Simple ping endpoint
                                "/error"                 // Error handling
                        ).permitAll()

                        // Category GET endpoints (public access)
                        .requestMatchers(HttpMethod.GET, "/v1/categories").permitAll()                    // Get all categories
                        .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()                 // Get category by ID and slug
                        .requestMatchers(HttpMethod.GET, "/v1/categories/active").permitAll()             // Get active categories
                        .requestMatchers(HttpMethod.GET, "/v1/categories/search").permitAll()             // Search categories
                        .requestMatchers(HttpMethod.GET, "/v1/categories/active/ordered").permitAll()     // Get active categories ordered

                        // Manufacturer GET endpoints (public access) - CORRECTED
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers").permitAll()                 // Get all manufacturers
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers/active").permitAll()          // Get active manufacturers
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers/categories").permitAll()      // Get manufacturers by categories
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers/categories/active").permitAll() // Get active manufacturers by categories
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers/categories/ordered").permitAll() // Get manufacturers by categories ordered
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers/search").permitAll()          // Search manufacturers
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers/slug/**").permitAll()         // Get the manufacturer by slug
                        .requestMatchers(HttpMethod.GET, "/v1/manufacturers/**").permitAll()              // Get the manufacturer by ID (this should be last)

                        // Compatibility Brand GET endpoints (public access)
                        .requestMatchers(HttpMethod.GET, "/v1/compatibility-brands").permitAll()                        // Get all compatibility brands
                        .requestMatchers(HttpMethod.GET, "/v1/compatibility-brands/slug/**").permitAll()                // Get compatibility brand by slug
                        .requestMatchers(HttpMethod.GET, "/v1/compatibility-brands/search").permitAll()                 // Search compatibility brands
                        .requestMatchers(HttpMethod.GET, "/v1/compatibility-brands/**").permitAll()                     // Get compatibility brand by ID

                        // Product GET endpoints (public access)
                        .requestMatchers(HttpMethod.GET, "/v1/products/slug/**").permitAll()              // Get product by slug
                        .requestMatchers(HttpMethod.GET, "/v1/products/sku/**").permitAll()               // Get product by SKU
                        .requestMatchers(HttpMethod.GET, "/v1/products/search").permitAll()               // Search products with filters
                        .requestMatchers(HttpMethod.GET, "/v1/products/images").permitAll()               // Get product image presigned URL
                        .requestMatchers(HttpMethod.GET, "/v1/products/*/variants").permitAll()           // Get product variants
                        .requestMatchers(HttpMethod.GET, "/v1/products/**").permitAll()                   // Get product by ID

                        // Search Endpoint
                        .requestMatchers(HttpMethod.GET, "/v1/search").permitAll()                        // Global Search

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "https://ssew-dashboard.pages.dev",
                "https://srishastabangalore.in",
                "https://dash.srishastabangalore.in",
                "https://www.srishastabangalore.in",
                "https://*.srishastabangalore.in"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}