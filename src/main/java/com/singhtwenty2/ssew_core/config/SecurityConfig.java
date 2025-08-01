package com.singhtwenty2.ssew_core.config;

import com.singhtwenty2.ssew_core.security.JwtAuthFilter;
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
                                "/v1/auth/**",           // All auth endpoints
                                "/v1/images/requirements/**", // Image requirement endpoints
                                "/v1/public/**",         // Public API endpoints
                                "/v1/health",            // Health check
                                "/actuator/health",      // Spring Boot health actuator
                                "/actuator/info",        // Application info
                                "/api/docs/**",          // API documentation
                                "/swagger-ui/**",        // Swagger UI
                                "/v3/api-docs/**",       // OpenAPI docs
                                "/favicon.ico",          // Favicon
                                "/error"                 // Error handling
                        ).permitAll()

                        // Category GET endpoints (public access)
                        .requestMatchers(HttpMethod.GET, "/v1/categories").permitAll()                    // Get all categories
                        .requestMatchers(HttpMethod.GET, "/v1/categories/*").permitAll()                  // Get category by ID
                        .requestMatchers(HttpMethod.GET, "/v1/categories/slug/*").permitAll()             // Get category by slug
                        .requestMatchers(HttpMethod.GET, "/v1/categories/active").permitAll()             // Get active categories
                        .requestMatchers(HttpMethod.GET, "/v1/categories/search").permitAll()             // Search categories
                        .requestMatchers(HttpMethod.GET, "/v1/categories/active/ordered").permitAll()     // Get active categories ordered

                        // Brand GET endpoints (public access)
                        .requestMatchers(HttpMethod.GET, "/v1/brands").permitAll()                        // Get all brands
                        .requestMatchers(HttpMethod.GET, "/v1/brands/*").permitAll()                      // Get brand by ID
                        .requestMatchers(HttpMethod.GET, "/v1/brands/slug/*").permitAll()                 // Get brand by slug
                        .requestMatchers(HttpMethod.GET, "/v1/brands/active").permitAll()                 // Get active brands
                        .requestMatchers(HttpMethod.GET, "/v1/brands/category/*").permitAll()             // Get brands by category
                        .requestMatchers(HttpMethod.GET, "/v1/brands/category/*/active").permitAll()      // Get active brands by category
                        .requestMatchers(HttpMethod.GET, "/v1/brands/category/*/ordered").permitAll()     // Get brands by category ordered
                        .requestMatchers(HttpMethod.GET, "/v1/brands/search").permitAll()                 // Search brands

                        // Public/Unauthenticated Product endpoints
                        .requestMatchers(HttpMethod.GET, "/v1/products/{productId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/slug/{slug}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/sku/{sku}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/status/{status}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/featured/status/{status}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/brand/{brandId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/brand/{brandId}/status/{status}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/search").permitAll()

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
                "http://localhost:3000",    // Local development
                "http://localhost:5173",    // Vite dev server
                "https://ssew.com",         // Production frontend domain
                "https://www.ssew.com",     // Production frontend domain with www
                "https://*.ssew.com"        // Subdomains if needed
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