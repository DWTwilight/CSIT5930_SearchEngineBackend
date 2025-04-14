package com.hkust.csit5930.searchengine.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import java.time.Duration;
import java.time.Instant;

@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private final ThreadLocal<Instant> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        startTime.set(Instant.now());
        String fullUrl = request.getRequestURL() +
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        log.info("Request: [{} {}]",
                request.getMethod(),
                fullUrl);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        Duration duration = Duration.between(startTime.get(), Instant.now());
        startTime.remove();
        String fullUrl = request.getRequestURL() +
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        log.info("Response: [{} {}] | Status: {} | Time: {}ms",
                request.getMethod(),
                fullUrl,
                response.getStatus(),
                duration.toMillis());
    }
}
