package com.singhtwenty2.ssew_core.security;

import com.singhtwenty2.ssew_core.data.entity.User;
import com.singhtwenty2.ssew_core.data.repository.UserRepository;
import com.singhtwenty2.ssew_core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

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

            if (!StringUtils.hasText(userId)) {
                filterChain.doFilter(request, response);
                return;
            }

            authenticateUser(userId, request);

        } catch (Exception e) {
            log.debug("Authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String userId, HttpServletRequest request) {
        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));

        if (userOptional.isEmpty()) {
            log.debug("User not found with ID: {}", userId);
            return;
        }

        User user = userOptional.get();

        if (!user.canLogin()) {
            log.debug("User cannot login - account inactive or locked: {}", userId);
            return;
        }

        List<SimpleGrantedAuthority> authorities = user.getRole().getPermissions()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                authorities
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v1/auth/") ||
                path.startsWith("/v1/public/") ||
                path.equals("/v1/health") ||
                path.equals("/actuator/health");
    }
}