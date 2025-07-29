package com.singhtwenty2.ssew_core.security;

import com.singhtwenty2.ssew_core.data.enums.UserRole;
import com.singhtwenty2.ssew_core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            if (!jwtService.validateAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String userId = jwtService.getUserIdFromAccessToken(token);
            UserRole userRole = jwtService.getUserRoleFromAccessToken(token);

            if (!StringUtils.hasText(userId) || userRole == null) {
                filterChain.doFilter(request, response);
                return;
            }

            authenticateUser(userId, userRole, request);
        } catch (Exception e) {
            log.debug("Authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(
            String userId,
            UserRole userRole,
            HttpServletRequest request) {
        try {
            UUID userUuid = UUID.fromString(userId);
            PrincipalUser principalUser = PrincipalUser
                    .builder()
                    .userId(userUuid)
                    .role(userRole)
                    .build();

            Set<String> permissions = userRole.getPermissions();

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principalUser,
                    null,
                    authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("User authenticated successfully: {} with role: {}", userId, userRole);

        } catch (IllegalArgumentException e) {
            log.debug("Invalid user ID format: {}", userId);
        } catch (Exception e) {
            log.debug("Error during user authentication: {}", e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v1/public/") ||
               path.equals("/v1/health") ||
               path.equals("/actuator/health");
    }
}