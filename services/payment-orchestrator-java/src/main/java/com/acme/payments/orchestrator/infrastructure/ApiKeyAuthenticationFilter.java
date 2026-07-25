package com.acme.payments.orchestrator.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final String apiKey;

    public ApiKeyAuthenticationFilter(@Value("${payments.security.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return apiKey == null || apiKey.isBlank() || request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!apiKey.equals(request.getHeader(API_KEY_HEADER))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid internal api key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
