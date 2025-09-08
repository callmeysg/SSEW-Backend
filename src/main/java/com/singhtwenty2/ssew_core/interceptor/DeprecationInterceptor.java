package com.singhtwenty2.ssew_core.interceptor;

import com.singhtwenty2.ssew_core.annotation.ApiDeprecated;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

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